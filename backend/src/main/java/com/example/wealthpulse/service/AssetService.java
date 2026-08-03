package com.example.wealthpulse.service;

import com.example.wealthpulse.model.Asset;
import com.example.wealthpulse.model.MetalAsset;
import com.example.wealthpulse.model.StockAsset;
import com.example.wealthpulse.repository.AssetRepository;
import com.example.wealthpulse.repository.MarketCacheRepository;
import com.example.wealthpulse.repository.HistoricalCacheRepository;
import com.example.wealthpulse.model.HistoricalCache;
import com.example.wealthpulse.model.MarketCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AssetService — implements the Cache Verification Pipeline from the design
 * document.
 *
 * Process Flow (as defined in the architecture paper):
 * 1. Query the market_cache table for the ticker
 * 2. If cache entry exists and lastUpdated < 15 minutes → use cached price
 * (skip API call)
 * 3. If stale or absent → call Alpha Vantage / GoldAPI via HTTPS
 * 4. Write fresh price + timestamp to market_cache table
 * 5. Return price for asset valuation
 *
 * The scheduled task runs every 4 hours and processes all tracked assets,
 * respecting the 15-minute TTL and free-tier API rate limits.
 */
@Service
public class AssetService {

    /** 15-minute cache TTL in minutes — matches the design specification. */
    public static final long CACHE_TTL_MINUTES = 15;
    private static final Duration API_REQUEST_DELAY = Duration.ofMillis(500);
    private static final Set<String> METAL_MARKETS = Set.of("XAU", "XAG", "XPT", "XPD");
    private static final Logger log = LoggerFactory.getLogger(AssetService.class);

    private final AssetRepository repository;
    private final MarketCacheRepository cacheRepository;
    private final HistoricalCacheRepository historicalCacheRepository;
    private final StockService stockService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${goldapi.key:${GOLD_API_KEY:}}")
    private String goldApiKey;

    public AssetService(AssetRepository repository,
            MarketCacheRepository cacheRepository,
            HistoricalCacheRepository historicalCacheRepository,
            StockService stockService) {
        this.repository = repository;
        this.cacheRepository = cacheRepository;
        this.historicalCacheRepository = historicalCacheRepository;
        this.stockService = stockService;

        // Configure RestTemplate with explicit read/connect timeouts to prevent
        // infinite blocking
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds maximum to connect
        factory.setReadTimeout(5000); // 5 seconds maximum to read data
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * Returns historical daily adjusted close series for the given ticker using a
     * server-side cache.
     * Cache TTL is 24 hours to avoid frequent large downloads.
     */
    public Map<String, Double> getHistoricalWithCache(String ticker) {
        String key = normalizeSymbol(ticker);
        Optional<HistoricalCache> cached = historicalCacheRepository.findByTickerSymbol(key);

        if (cached.isPresent() && cached.get().getLastUpdated().isAfter(LocalDateTime.now().minusHours(24))) {
            return parseHistoricalPayload(cached.get()).orElseGet(() -> refreshHistoricalPrices(key, ticker, cached));
        }

        return refreshHistoricalPrices(key, ticker, cached);
    }

    private Map<String, Double> refreshHistoricalPrices(String key, String ticker,
            Optional<HistoricalCache> cached) {
        if (isMetalTicker(key)) {
            throw new IllegalStateException(
                    "Historical metal data is unavailable; synthetic prices are not used for performance");
        }

        Map<String, Double> fresh = stockService.getHistoricalDailyAdjusted(ticker);
        try {
            String payload = objectMapper.writeValueAsString(fresh);
            HistoricalCache entry = cached.orElseGet(() -> new HistoricalCache(key, payload));
            entry.setPayload(payload);
            entry.setLastUpdated(LocalDateTime.now());
            historicalCacheRepository.save(entry);
        } catch (DataAccessException e) {
            log.warn("Unable to cache historical prices for ticker={}", key, e);
        } catch (Exception e) {
            log.warn("Unable to serialize historical prices for ticker={}", key, e);
        }
        return fresh;
    }

    private Optional<Map<String, Double>> parseHistoricalPayload(HistoricalCache cached) {
        try {
            return Optional.of(objectMapper.readValue(cached.getPayload(), new TypeReference<Map<String, Double>>() {
            }));
        } catch (Exception e) {
            log.warn("Corrupt historical cache payload for ticker={}", cached.getTickerSymbol(), e);
            return Optional.empty();
        }
    }

    private boolean isMetalTicker(String symbol) {
        return METAL_MARKETS.contains(symbol) || symbol.contains("GOLD") || symbol.contains("SILVER");
    }

    private String normalizeSymbol(String symbol) {
        return symbol == null ? "" : symbol.toUpperCase().trim();
    }

    /**
     * Scheduled price sync — runs once every 4 hours to refresh asset pricing
     * benchmarks.
     *
     * Readability note:
     * - This method iterates over all tracked assets in the DB.
     * - For each asset, it uses the *cache-first* strategy before calling an
     * external API.
     * - External API calls are rate-limited using a small sleep between requests.
     */
    @Scheduled(fixedRate = 14400000)
    public void updateAssetPrices() {
        List<Asset> assets = repository.findAll();
        Map<String, Double> metalPrices = new HashMap<>();

        for (Asset asset : assets) {
            try {
                refreshPrice(asset, metalPrices);
                pauseForRateLimit();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                log.info("Scheduled price refresh interrupted");
                return;
            } catch (RuntimeException exception) {
                log.warn("Price refresh failed for asset id={}", asset.getId(), exception);
            }
        }
    }

    private void refreshPrice(Asset asset, Map<String, Double> metalPrices) {
        if (asset instanceof MetalAsset metal) {
            String symbol = getMetalMarketSymbol(metal);
            metal.setPrice(metalPrices.computeIfAbsent(symbol, this::getMetalPriceWithCache));
            repository.save(metal);
            return;
        }
        if (asset instanceof StockAsset stock
                && !"BOND".equalsIgnoreCase(stock.getAssetSubType())
                && stock.getTicker() != null
                && !stock.getTicker().isBlank()) {
            stock.setPrice(getStockPriceWithCache(stock.getTicker().trim()));
            repository.save(stock);
            return;
        }
        log.debug("Skipping unsupported or non-market asset id={}", asset.getId());
    }

    private void pauseForRateLimit() throws InterruptedException {
        Thread.sleep(API_REQUEST_DELAY.toMillis());
    }

    /**
     * Stock price fetch with cache verification.
     *
     * Checks the market_cache table first. If entry is fresh (< 15 min),
     * returns cached price without consuming an API call.
     */
    public double getStockPriceWithCache(String ticker) {
        String symbol = normalizeSymbol(ticker);
        Optional<MarketCache> cached = cacheRepository.findByTickerSymbol(symbol);

        if (cached.filter(this::isCacheFresh).filter(this::hasValidSpotPrice).isPresent()) {
            log.debug("Using cached stock price for {} (age: {} min)", symbol, getCacheAgeMinutes(cached.get()));
            return cached.get().getSpotPrice().doubleValue();
        }

        return fetchStockPrice(symbol, cached);
    }

    private double fetchStockPrice(String symbol, Optional<MarketCache> cached) {
        log.debug("Fetching stock price from Alpha Vantage for {}", symbol);
        try {
            double freshPrice = stockService.getStockPrice(symbol);
            if (freshPrice > 0) {
                writePriceToCache(symbol, freshPrice);
                return freshPrice;
            }
            throw new RuntimeException("Provider returned no usable price");
        } catch (Exception ex) {
            if (cached.filter(this::hasValidSpotPrice).isPresent()) {
                log.warn("Using stale cached stock price for {} after provider failure", symbol);
                return cached.get().getSpotPrice().doubleValue();
            }
            throw new RuntimeException("Stock price fetch failed for " + symbol + ": " + ex.getMessage(), ex);
        }
    }

    private boolean hasValidSpotPrice(MarketCache entry) {
        return entry.getSpotPrice() != null && entry.getSpotPrice().doubleValue() > 0;
    }

    /**
     * Checks whether the cache entry is within the 15-minute TTL window.
     */
    public boolean isCacheFresh(MarketCache entry) {
        return entry.getLastUpdated().isAfter(LocalDateTime.now().minusMinutes(CACHE_TTL_MINUTES));
    }

    /**
     * Returns the age of a cache entry in minutes (for status reporting).
     */
    public long getCacheAgeMinutes(MarketCache entry) {
        return java.time.Duration.between(entry.getLastUpdated(), LocalDateTime.now()).toMinutes();
    }

    /**
     * Write or update a price entry in the market_cache table.
     */
    public void writePriceToCache(String tickerSymbol, double price) {
        // If the cache row doesn't exist yet, this creates a new MarketCache entity.
        // (In a future refactor, you could also enforce uniqueness in the DB and/or
        // add an upsert repository method.)
        MarketCache entry = cacheRepository.findByTickerSymbol(tickerSymbol)
                .orElse(new MarketCache());
        entry.setTickerSymbol(tickerSymbol);
        entry.setSpotPrice(BigDecimal.valueOf(price));
        entry.setLastUpdated(LocalDateTime.now());
        cacheRepository.save(entry);
    }

    public double getMetalPriceWithCache(String symbol) {
        String apiSymbol = normalizeMetalSymbol(symbol);
        Optional<MarketCache> cached = cacheRepository.findByTickerSymbol(apiSymbol);

        if (cached.filter(this::isCacheFresh).filter(this::hasValidSpotPrice).isPresent()) {
            log.debug("Using cached metal price for {}", apiSymbol);
            return cached.get().getSpotPrice().doubleValue();
        }

        return fetchMetalPrice(apiSymbol, cached);
    }

    private double fetchMetalPrice(String apiSymbol, Optional<MarketCache> cached) {
        log.debug("Fetching metal price from GoldAPI for {}", apiSymbol);
        String url = "https://www.goldapi.io/api/" + apiSymbol + "/USD";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-access-token", goldApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoldApiResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, GoldApiResponse.class);

            if (response.getBody() != null && response.getBody().price > 0) {
                double price = response.getBody().price;
                writePriceToCache(apiSymbol, price);
                log.debug("Updated cached metal price for {}", apiSymbol);
                return price;
            }
            throw new RuntimeException("Empty price payload from GoldAPI for: " + apiSymbol);
        } catch (Exception e) {
            if (cached.filter(this::hasValidSpotPrice).isPresent()) {
                log.warn("Using stale cached metal price for {} after provider failure", apiSymbol);
                return cached.get().getSpotPrice().doubleValue();
            }
            throw new RuntimeException("GoldAPI fetch failed for " + apiSymbol + ": " + e.getMessage(), e);
        }
    }

    /**
     * Resolves a physical product to its market symbol. The persisted ticker is
     * authoritative when it is a valid metal market symbol; otherwise the
     * product name is classified ("Silver Bar" -> XAG, for example).
     */
    public String getMetalMarketSymbol(MetalAsset asset) {
        String ticker = asset.getTicker();
        if (ticker != null && ticker.matches("(?i)AU|XAU|AG|XAG|PT|XPT|PD|XPD")) {
            return normalizeMetalSymbol(ticker);
        }
        return normalizeMetalSymbol(asset.getName());
    }

    public String normalizeMetalSymbol(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return "XAU";
        }
        String upper = symbol.toUpperCase().trim();
        if (upper.contains("GOLD") || upper.equals("AU") || upper.equals("XAU")
                || upper.equals("BULLION")) {
            return "XAU";
        }
        if (upper.contains("SILVER") || upper.equals("AG") || upper.equals("XAG")) {
            return "XAG";
        }
        if (upper.contains("PLATINUM") || upper.equals("PT") || upper.equals("XPT")) {
            return "XPT";
        }
        if (upper.contains("PALLADIUM") || upper.equals("PD") || upper.equals("XPD")) {
            return "XPD";
        }
        return upper;
    }
}
