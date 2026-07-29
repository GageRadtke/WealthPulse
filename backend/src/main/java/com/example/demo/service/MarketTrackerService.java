package com.example.demo.service;

import com.example.demo.model.MarketCache;
import com.example.demo.repository.MarketCacheRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MarketTrackerService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MarketTrackerService.class);
    private final MarketCacheRepository cacheRepository;

    private static final long QUOTE_REFRESH_MS = 60 * 60 * 1000L;

    public MarketTrackerService(MarketCacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    @Scheduled(fixedRate = QUOTE_REFRESH_MS)
    public void refreshMarketQuotes() {
        for (QuoteSymbol symbol : QuoteSymbol.values()) {
            refreshQuote(symbol);
        }
    }

    public List<Map<String, Object>> getMarketQuotes() {
        List<Map<String, Object>> quotes = new ArrayList<>();

        for (QuoteSymbol symbol : QuoteSymbol.values()) {
            try {
                Optional<MarketCache> cacheEntry = cacheRepository.findByTickerSymbol(symbol.getTicker().toUpperCase());
                if (cacheEntry.isEmpty() || cacheEntry.get().getLastUpdated()
                        .isBefore(LocalDateTime.now().minusHours(1))) {
                    refreshQuote(symbol);
                    cacheEntry = cacheRepository.findByTickerSymbol(symbol.getTicker().toUpperCase());
                }

                Double price = cacheEntry.map(entry -> entry.getSpotPrice().doubleValue()).orElse(null);
                String status = price == null ? "unavailable" : "ok";
                LocalDateTime lastUpdated = LocalDateTime.now();
                if (cacheEntry.isPresent()) {
                    lastUpdated = cacheEntry.get().getLastUpdated();
                }

                Map<String, Object> quote = new java.util.HashMap<>();
                quote.put("ticker", symbol.getTicker());
                quote.put("name", symbol.getDisplayName());
                quote.put("description", symbol.getDescription());
                quote.put("price", price);        // may be null if API call failed
                quote.put("status", status);
                quote.put("lastUpdated", lastUpdated.toString());
                quotes.add(quote);

            } catch (Exception outerEx) {
                log.warn("Skipping market tracker symbol {}", symbol.getTicker(), outerEx);
            }
        }

        return quotes;
    }

    /** Scrapes the public Yahoo Finance quote page and persists one hourly quote. */
    private void refreshQuote(QuoteSymbol symbol) {
        try {
            String encodedTicker = URLEncoder.encode(symbol.getTicker(), StandardCharsets.UTF_8);
            URI uri = URI.create("https://finance.yahoo.com/quote/" + encodedTicker);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 WealthPulse Market Tracker");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            StringBuilder page = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) page.append(line);
            }

            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("\\\"regularMarketPrice\\\":\\{\\\"raw\\\":([0-9.]+)")
                    .matcher(page);
            if (!matcher.find()) throw new IllegalStateException("Quote price was not present in source page");

            MarketCache entry = cacheRepository.findByTickerSymbol(symbol.getTicker().toUpperCase())
                    .orElse(new MarketCache());
            entry.setTickerSymbol(symbol.getTicker().toUpperCase());
            entry.setSpotPrice(new BigDecimal(matcher.group(1)));
            entry.setLastUpdated(LocalDateTime.now());
            cacheRepository.save(entry);
        } catch (Exception ex) {
            log.warn("Market tracker web quote refresh failed for {}", symbol.getTicker(), ex);
        }
    }

    private enum QuoteSymbol {
        SP500("^GSPC", "S&P 500", "Tracks the S&P 500 benchmark."),
        DJIA("^DJI", "DJIA", "Tracks the Dow Jones Industrial Average."),
        NASDAQ("^IXIC", "NASDAQ", "Tracks the NASDAQ index."),
        RUSSELL2000("^RUT", "Russell 2000", "Tracks smaller U.S. public companies.");

        private final String ticker;
        private final String name;
        private final String description;

        QuoteSymbol(String ticker, String name, String description) {
            this.ticker = ticker;
            this.name = name;
            this.description = description;
        }

        public String getTicker() {
            return ticker;
        }

        public String getDisplayName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}
