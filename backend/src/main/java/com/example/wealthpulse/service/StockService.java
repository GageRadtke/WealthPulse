package com.example.wealthpulse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
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

    /** Loads company ratios and dividend history used by the stock analytics UI. */
    @SuppressWarnings("unchecked")
    public StockFundamentals getStockFundamentals(String ticker) {
        requireApiKey();
        Map<String, Object> overview = requestMap("OVERVIEW", ticker);
        rejectProviderError(overview, ticker);

        Double dividendPerShare = decimal(overview.get("DividendPerShare"));
        Double dividendYield = decimal(overview.get("DividendYield"));
        Double payoutRatio = decimal(overview.get("PayoutRatio"));
        String sector = text(overview.get("Sector"));

        List<StockFundamentals.DividendPayment> dividends = List.of();
        if (dividendPerShare != null && dividendPerShare > 0) {
            try {
                Map<String, Object> response = requestMap("DIVIDENDS", ticker);
                rejectProviderError(response, ticker);
                dividends = parseDividends(response.get("data"));
            } catch (RuntimeException exception) {
                // Overview data is still useful when the separate history request is
                // rate-limited or unavailable. CAGR remains unknown until a later refresh.
                log.warn("Dividend history unavailable for {}; saving overview data", ticker);
            }
        }
        return new StockFundamentals(sector, dividendPerShare, dividendYield, payoutRatio, dividends);
    }

    private Map<String, Object> requestMap(String function, String ticker) {
        String url = "https://www.alphavantage.co/query?function=" + function + "&symbol="
                + ticker + "&apikey=" + apiKey;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null) {
            throw new IllegalStateException("Empty Alpha Vantage response for " + ticker);
        }
        return response;
    }

    private void rejectProviderError(Map<String, Object> response, String ticker) {
        Object message = response.get("Error Message");
        if (message == null) message = response.get("Note");
        if (message == null) message = response.get("Information");
        if (message != null) {
            throw new IllegalStateException("Alpha Vantage could not return fundamentals for " + ticker);
        }
    }

    @SuppressWarnings("unchecked")
    private List<StockFundamentals.DividendPayment> parseDividends(Object value) {
        if (!(value instanceof List<?> rows)) {
            return List.of();
        }
        List<StockFundamentals.DividendPayment> payments = new ArrayList<>();
        for (Object row : rows) {
            if (!(row instanceof Map<?, ?> map)) continue;
            try {
                LocalDate date = LocalDate.parse(String.valueOf(map.get("ex_dividend_date")));
                Double amount = decimal(map.get("amount"));
                if (amount != null && amount >= 0) {
                    payments.add(new StockFundamentals.DividendPayment(date, amount));
                }
            } catch (RuntimeException ignored) {
                // Ignore one malformed provider row without losing valid history.
            }
        }
        return payments;
    }

    private void requireApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Alpha Vantage API key is not configured");
        }
    }

    private Double decimal(Object value) {
        if (value == null) return null;
        String text = value.toString().trim();
        if (text.isEmpty() || "None".equalsIgnoreCase(text) || "-".equals(text)) return null;
        try {
            return Double.valueOf(text);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String text(Object value) {
        if (value == null || value.toString().isBlank() || "None".equalsIgnoreCase(value.toString())) {
            return null;
        }
        return value.toString().trim();
    }

    /**
     * Fetches daily adjusted historical close prices for a ticker using Alpha Vantage,
     * falling back to TIME_SERIES_DAILY when adjusted data is unavailable.
     * Missing market history is reported to callers instead of being fabricated.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Double> getHistoricalDailyAdjusted(String ticker) {
        Map<String, Double> sp500History = getSp500History(ticker);
        if (!sp500History.isEmpty()) {
            log.info("Using Federal Reserve S&P 500 history for {}", ticker);
            return sp500History;
        }

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

    private Map<String, Double> getSp500History(String ticker) {
        if (!"SPY".equalsIgnoreCase(ticker)) {
            return Map.of();
        }
        try {
            String url = "https://fred.stlouisfed.org/graph/fredgraph.csv?id=SP500";
            return parseFredCsv(restTemplate.getForObject(url, String.class));
        } catch (RuntimeException exception) {
            log.warn("Federal Reserve S&P 500 historical query failed", exception);
            return Map.of();
        }
    }

    static Map<String, Double> parseFredCsv(String csv) {
        Map<String, Double> prices = new TreeMap<>();
        if (csv == null || csv.isBlank()) return prices;
        String[] lines = csv.split("\\R");
        for (int index = 1; index < lines.length; index++) {
            String[] columns = lines[index].split(",");
            if (columns.length < 2) continue;
            try {
                LocalDate.parse(columns[0]);
                double close = Double.parseDouble(columns[1]);
                if (close > 0) prices.put(columns[0], close);
            } catch (RuntimeException ignored) {
                // Ignore malformed rows while retaining valid daily closes.
            }
        }
        return prices;
    }
}
