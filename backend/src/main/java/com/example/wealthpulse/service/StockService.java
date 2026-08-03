package com.example.wealthpulse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.util.Map;
import java.util.TreeMap;

@Service
public class StockService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StockService.class);

    @Value("${alphavantage.key:${ALPHA_VANTAGE_KEY:}}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public StockService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    public double getStockPrice(String ticker) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException(
                    "AlphaVantage API Key variable is missing from your .env configuration file.");
        }

        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + ticker + "&apikey=" + apiKey;
        AlphaVantageResponse response = restTemplate.getForObject(url, AlphaVantageResponse.class);

        if (response != null && response.getGlobalQuote() != null && response.getGlobalQuote().getPrice() != null) {
            return Double.parseDouble(response.getGlobalQuote().getPrice());
        }

        throw new RuntimeException("No valid quote payload returned for ticker: " + ticker);
    }

    /**
     * Fetches daily adjusted historical close prices for a ticker using Alpha Vantage,
     * falling back to TIME_SERIES_DAILY when adjusted data is unavailable.
     * Missing market history is reported to callers instead of being fabricated.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Double> getHistoricalDailyAdjusted(String ticker) {
        if (apiKey != null && !apiKey.isEmpty()) {
            String[] functions = new String[] { "TIME_SERIES_DAILY_ADJUSTED", "TIME_SERIES_DAILY" };
            for (String fn : functions) {
                try {
                    String url = "https://www.alphavantage.co/query?function=" + fn + "&symbol="
                            + ticker + "&outputsize=full&apikey=" + apiKey;

                    Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                    if (response != null && response.containsKey("Time Series (Daily)")) {
                        Object seriesObj = response.get("Time Series (Daily)");
                        if (seriesObj instanceof Map) {
                            Map<String, Object> series = (Map<String, Object>) seriesObj;
                            Map<String, Double> results = new TreeMap<>();
                            for (Map.Entry<String, Object> entry : series.entrySet()) {
                                String date = entry.getKey();
                                Object dayObj = entry.getValue();
                                if (dayObj instanceof Map) {
                                    Map<String, Object> dayMap = (Map<String, Object>) dayObj;
                                    Object adjClose = dayMap.get("5. adjusted close");
                                    if (adjClose == null) {
                                        adjClose = dayMap.get("4. close");
                                    }
                                    if (adjClose != null) {
                                        try {
                                            results.put(date, Double.parseDouble(adjClose.toString()));
                                        } catch (Exception ex) {
                                            // ignore malformed price
                                        }
                                    }
                                }
                            }
                            if (!results.isEmpty()) {
                                return results;
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.warn("Alpha Vantage historical query failed for {} ({})", ticker, fn, ex);
                }
            }
        }

        throw new IllegalStateException("Historical market data is unavailable for " + ticker);
    }
}
