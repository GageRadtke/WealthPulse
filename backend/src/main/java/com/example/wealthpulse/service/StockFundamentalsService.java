package com.example.wealthpulse.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.wealthpulse.model.StockAsset;
import com.example.wealthpulse.model.User;
import com.example.wealthpulse.repository.AssetRepository;

@Service
public class StockFundamentalsService {
    private static final Logger log = LoggerFactory.getLogger(StockFundamentalsService.class);
    private static final int CACHE_HOURS = 24;

    private final AssetRepository assetRepository;
    private final StockService stockService;

    public StockFundamentalsService(AssetRepository assetRepository, StockService stockService) {
        this.assetRepository = assetRepository;
        this.stockService = stockService;
    }

    public RefreshResult refreshForUser(User user, boolean force) {
        return refresh(assetRepository.findStockAssetsByOwnerId(user.getId()), force);
    }

    @Scheduled(cron = "0 30 2 * * *")
    public void refreshAllDaily() {
        refresh(assetRepository.findAllStockAssetsWithTicker(), false);
    }

    private RefreshResult refresh(List<StockAsset> stocks, boolean force) {
        Map<String, List<StockAsset>> byTicker = groupByTicker(stocks);
        int updated = 0;
        int cached = 0;
        List<String> failed = new ArrayList<>();

        for (Map.Entry<String, List<StockAsset>> entry : byTicker.entrySet()) {
            String ticker = entry.getKey();
            List<StockAsset> positions = entry.getValue();
            if (!force && positions.stream().anyMatch(this::isFresh)) {
                cached++;
                continue;
            }
            try {
                StockFundamentals fundamentals = stockService.getStockFundamentals(ticker);
                updatePositions(positions, fundamentals);
                updated++;
            } catch (RuntimeException exception) {
                failed.add(ticker);
                log.warn("Fundamentals refresh failed for {}", ticker, exception);
            }
        }
        return new RefreshResult(byTicker.size(), updated, cached, failed);
    }

    private Map<String, List<StockAsset>> groupByTicker(List<StockAsset> stocks) {
        Map<String, List<StockAsset>> grouped = new LinkedHashMap<>();
        for (StockAsset stock : stocks) {
            if ("BOND".equalsIgnoreCase(stock.getAssetSubType())) continue;
            String ticker = stock.getTicker().trim().toUpperCase();
            grouped.computeIfAbsent(ticker, ignored -> new ArrayList<>()).add(stock);
        }
        return grouped;
    }

    private boolean isFresh(StockAsset stock) {
        return stock.getFundamentalsUpdatedAt() != null
                && stock.getFundamentalsUpdatedAt().isAfter(LocalDateTime.now().minusHours(CACHE_HOURS));
    }

    private void updatePositions(List<StockAsset> positions, StockFundamentals fundamentals) {
        Double dividendCagr = calculateFiveYearDividendCagr(fundamentals.dividends(), LocalDate.now());
        LocalDateTime now = LocalDateTime.now();
        for (StockAsset stock : positions) {
            if (fundamentals.sector() != null) stock.setSector(fundamentals.sector());
            stock.setDivRate(fundamentals.dividendPerShare());
            stock.setDividendYield(fundamentals.dividendYield());
            stock.setPayoutRatio(fundamentals.payoutRatio());
            stock.setCagr5Yr(dividendCagr);
            stock.setFundamentalsUpdatedAt(now);
        }
        assetRepository.saveAll(positions);
    }

    static Double calculateFiveYearDividendCagr(List<StockFundamentals.DividendPayment> dividends,
            LocalDate today) {
        Map<Integer, Double> annualTotals = new TreeMap<>();
        for (StockFundamentals.DividendPayment payment : dividends) {
            annualTotals.merge(payment.exDividendDate().getYear(), payment.amount(), Double::sum);
        }
        int latestYear = today.getYear() - 1;
        double latest = annualTotals.getOrDefault(latestYear, 0d);
        double baseline = annualTotals.getOrDefault(latestYear - 5, 0d);
        if (latest <= 0 || baseline <= 0) return null;
        return Math.pow(latest / baseline, 1d / 5d) - 1d;
    }

    public record RefreshResult(int requested, int updated, int cached, List<String> failed) {
    }
}
