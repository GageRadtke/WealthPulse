package com.example.demo.service;

import com.example.demo.model.Asset;
import com.example.demo.model.MarketCache;
import com.example.demo.model.MetalAsset;
import com.example.demo.model.StockAsset;
import com.example.demo.repository.AssetRepository;
import com.example.demo.repository.MarketCacheRepository;
import com.example.demo.repository.HistoricalCacheRepository;
import com.example.demo.model.HistoricalCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private static final long CACHE_TTL_MINUTES = 15;
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
    public java.util.Map<String, Double> getHistoricalWithCache(String ticker) {
        String key = ticker.toUpperCase().trim();
        String normalizedMetal = normalizeMetalSymbol(key);
        boolean isMetal = key.equals("XAU") || key.equals("XAG") || key.equals("XPT") || key.equals("XPD")
                || key.contains("GOLD") || key.contains("SILVER");

        Optional<HistoricalCache> cached = historicalCacheRepository.findByTickerSymbol(key);
        if (cached.isPresent() && cached.get().getLastUpdated().isAfter(LocalDateTime.now().minusHours(24))) {
            try {
                return objectMapper.readValue(cached.get().getPayload(),
                        new TypeReference<java.util.Map<String, Double>>() {
                        });
            } catch (Exception e) {
                // fall through to refresh
            }
        }

        // Cache miss or parsing failure: fetch fresh series and persist
        java.util.Map<String, Double> fresh;
        if (isMetal) {
            throw new IllegalStateException(
                    "Historical metal data is unavailable; synthetic prices are not used for performance");
        } else {
            fresh = stockService.getHistoricalDailyAdjusted(ticker);
        }

        try {
            String payload = objectMapper.writeValueAsString(fresh);
            HistoricalCache entry = cached.orElseGet(() -> new HistoricalCache(key, payload));
            entry.setPayload(payload);
            entry.setLastUpdated(LocalDateTime.now());
            historicalCacheRepository.save(entry);
        } catch (Exception e) {
            // ignore persistence errors but return data
        }
        return fresh;
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
        java.util.Map<String, Double> metalPrices = new java.util.HashMap<>();

        for (Asset asset : assets) {
            try {
                // Type-based dispatch for the two supported asset models.
                // Note: this currently relies on runtime instances being either:
                // - StockAsset (uses ticker -> AlphaVantage)
                // - MetalAsset (uses ticker/name -> GoldAPI)
                // Type-based dispatch for the two supported asset models.
                // (This uses plain instanceof checks for compatibility with the project's
                // configured Java source level.)
                if (asset instanceof MetalAsset metal) {
                    String metalSymbol = getMetalMarketSymbol(metal);
                    // Fetch once per metal market (XAG, XAU, etc.) per refresh.
                    double price = metalPrices.computeIfAbsent(metalSymbol, this::getMetalPriceWithCache);
                    metal.setPrice(price);
                    repository.save(metal);
                } else if (asset instanceof StockAsset stock) {
                    if ("BOND".equalsIgnoreCase(stock.getAssetSubType())) {
                        continue;
                    }
                    if (stock.getTicker() == null || stock.getTicker().isBlank()) {
                        continue;
                    }
                    // Stock valuation: refresh benchmark price via cache pipeline.
                    double newPrice = getStockPriceWithCache(stock.getTicker().trim());
                    stock.setPrice(newPrice);
                    repository.save(stock);
                } else {
                    log.warn("Skipping unsupported asset type {}", asset.getClass().getSimpleName());
                }

                // 500ms pause to comply with free-tier API rate limits
                Thread.sleep(500);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Price update task was interrupted");
                break;
            } catch (Exception e) {
                log.warn("Skipping price update for asset {} ({})", asset.getName(), asset.getTicker(), e);
            }
        }
    }

    /**
     * Stock price fetch with cache verification.
     *
     * Checks the market_cache table first. If entry is fresh (< 15 min),
     * returns cached price without consuming an API call.
     */
    public double getStockPriceWithCache(String ticker) {
        Optional<MarketCache> cached = cacheRepository.findByTickerSymbol(ticker.toUpperCase());

        if (cached.isPresent() && isCacheFresh(cached.get()) && cached.get().getSpotPrice() != null
                && cached.get().getSpotPrice().doubleValue() > 0) {
            log.debug("Using cached stock price for {} (age: {} min)", ticker, getCacheAgeMinutes(cached.get()));
            return cached.get().getSpotPrice().doubleValue();
        }

        log.debug("Fetching stock price from Alpha Vantage for {}", ticker);
        try {
            double freshPrice = stockService.getStockPrice(ticker);
            if (freshPrice > 0) {
                writePriceToCache(ticker.toUpperCase(), freshPrice);
                return freshPrice;
            }
            throw new RuntimeException("Provider returned no usable price");
        } catch (Exception ex) {
            if (cached.isPresent() && cached.get().getSpotPrice() != null
                    && cached.get().getSpotPrice().doubleValue() > 0) {
                log.warn("Using stale cached stock price for {} after provider failure", ticker);
                return cached.get().getSpotPrice().doubleValue();
            }
            throw new RuntimeException("Stock price fetch failed for " + ticker + ": " + ex.getMessage(), ex);
        }
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
        Optional<MarketCache> cached = cacheRepository.findByTickerSymbol(apiSymbol.toUpperCase());

        if (cached.isPresent() && isCacheFresh(cached.get()) && cached.get().getSpotPrice() != null
                && cached.get().getSpotPrice().doubleValue() > 0) {
            log.debug("Using cached metal price for {}", apiSymbol);
            return cached.get().getSpotPrice().doubleValue();
        }

        log.debug("Fetching metal price from GoldAPI for {}", apiSymbol);
        String url = "https://www.goldapi.io/api/" + apiSymbol + "/USD";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-access-token", goldApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoldApiResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, GoldApiResponse.class);

            if (response.getBody() != null && response.getBody().price > 0) {
                // GoldAPI's XAU/XAG/XPT/XPD USD `price` is quoted per troy ounce.
                double price = response.getBody().price;
                writePriceToCache(apiSymbol.toUpperCase(), price);
                log.debug("Updated cached metal price for {}", apiSymbol);
                return price;
            }
            throw new RuntimeException("Empty price payload from GoldAPI for: " + symbol);
        } catch (Exception e) {
            // A stale cached quote is still more accurate than displaying a zero
            // valuation when the market provider is temporarily unavailable.
            if (cached.isPresent() && cached.get().getSpotPrice() != null
                    && cached.get().getSpotPrice().doubleValue() > 0) {
                log.warn("Using stale cached metal price for {} after provider failure", apiSymbol);
                return cached.get().getSpotPrice().doubleValue();
            }
            throw new RuntimeException("GoldAPI fetch failed for " + symbol + ": " + e.getMessage(), e);
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
