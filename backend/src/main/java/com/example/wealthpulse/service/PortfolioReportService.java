package com.example.wealthpulse.service;

import static com.example.wealthpulse.service.PortfolioLedgerService.decimal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.example.wealthpulse.model.Asset;
import com.example.wealthpulse.model.PortfolioSnapshot;
import com.example.wealthpulse.service.PortfolioMetricsService.PortfolioMetrics;

@Service
public class PortfolioReportService {
    private final AssetService assetService;

    public PortfolioReportService(AssetService assetService) {
        this.assetService = assetService;
    }

    public Map<String, Object> build(String period, String benchmark, LocalDate requestedStart,
            LocalDate end, List<PortfolioSnapshot> snapshots, PortfolioSnapshot current,
            List<Asset> assets, PortfolioMetrics metrics) {
        PortfolioSnapshot first = snapshots.get(0);
        double portfolioReturn = percent(
                metrics.currentValue().subtract(first.getTotalValue()), first.getTotalValue());
        BenchmarkResult benchmarkResult = loadBenchmark(benchmark, first.getSnapshotDate(), end);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("period", period);
        result.put("requestedStartDate", requestedStart);
        result.put("startDate", first.getSnapshotDate());
        result.put("endDate", end);
        result.put("historyComplete", !first.getSnapshotDate().isAfter(requestedStart));
        result.put("historyMessage", historyMessage(first.getSnapshotDate(), requestedStart));
        result.put("benchmark", benchmark);
        result.put("summary", summary(metrics, portfolioReturn, benchmarkResult));
        result.put("series", series(snapshots, benchmarkResult.prices()));
        result.put("allocation", Map.of("start", allocation(first), "end", allocation(current)));
        result.put("performers", performers(assets));
        return result;
    }

    private BenchmarkResult loadBenchmark(String symbol, LocalDate start, LocalDate end) {
        try {
            NavigableMap<LocalDate, Double> prices = new TreeMap<>();
            assetService.getHistoricalWithCache(symbol).forEach((date, price) -> addPrice(prices, date, price));
            Map.Entry<LocalDate, Double> first = prices.ceilingEntry(start);
            Map.Entry<LocalDate, Double> last = prices.floorEntry(end);
            if (first == null || last == null || !last.getKey().isAfter(first.getKey())) {
                return new BenchmarkResult(false, 0, prices);
            }
            return new BenchmarkResult(true, (last.getValue() / first.getValue() - 1) * 100, prices);
        } catch (RuntimeException exception) {
            return new BenchmarkResult(false, 0, new TreeMap<>());
        }
    }

    private void addPrice(NavigableMap<LocalDate, Double> prices, String date, Double price) {
        try {
            prices.put(LocalDate.parse(date), price);
        } catch (RuntimeException ignored) {
            // Ignore malformed dates returned by the external provider.
        }
    }

    private Map<String, Object> summary(PortfolioMetrics metrics, double portfolioReturn,
            BenchmarkResult benchmark) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("currentValue", money(metrics.currentValue()));
        summary.put("costBasis", money(metrics.costBasis()));
        summary.put("netContributions", money(metrics.contributions()));
        summary.put("investmentGrowth", money(metrics.growth()));
        summary.put("realizedGain", money(metrics.realizedGain()));
        summary.put("unrealizedGain", money(metrics.unrealizedGain()));
        summary.put("income", money(metrics.income()));
        summary.put("fees", money(metrics.fees()));
        summary.put("portfolioReturnPercent", round(portfolioReturn));
        summary.put("benchmarkReturnPercent", round(benchmark.returnPercent()));
        summary.put("benchmarkAvailable", benchmark.available());
        return summary;
    }

    private List<Map<String, Object>> series(List<PortfolioSnapshot> snapshots,
            NavigableMap<LocalDate, Double> benchmarkPrices) {
        List<Map<String, Object>> points = new ArrayList<>();
        BigDecimal portfolioBase = snapshots.get(0).getTotalValue();
        Map.Entry<LocalDate, Double> benchmarkBase = benchmarkPrices.ceilingEntry(snapshots.get(0).getSnapshotDate());

        for (PortfolioSnapshot snapshot : snapshots) {
            Map.Entry<LocalDate, Double> benchmarkPoint = benchmarkPrices.floorEntry(snapshot.getSnapshotDate());
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", snapshot.getSnapshotDate());
            point.put("portfolioValue", money(snapshot.getTotalValue()));
            point.put("portfolioIndex", index(snapshot.getTotalValue(), portfolioBase));
            point.put("benchmarkIndex", benchmarkBase == null || benchmarkPoint == null
                    ? null : round(benchmarkPoint.getValue() / benchmarkBase.getValue() * 100));
            points.add(point);
        }
        return points;
    }

    private Map<String, Object> performers(List<Asset> assets) {
        List<Map<String, Object>> ranked = assets.stream()
                .filter(asset -> decimal(asset.getAmountPaid()).signum() > 0)
                .map(this::performer)
                .sorted(Comparator.comparingDouble(row -> -((Number) row.get("returnPercent")).doubleValue()))
                .toList();
        List<Map<String, Object>> worst = ranked.stream()
                .sorted(Comparator.comparingDouble(row -> ((Number) row.get("returnPercent")).doubleValue()))
                .limit(3).toList();
        return Map.of("best", ranked.stream().limit(3).toList(), "worst", worst);
    }

    private Map<String, Object> performer(Asset asset) {
        BigDecimal value = decimal(asset.getQuantity()).multiply(decimal(asset.getPrice()));
        BigDecimal basis = decimal(asset.getAmountPaid());
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("symbol", asset.getTicker() == null ? asset.getName() : asset.getTicker());
        row.put("name", asset.getName());
        row.put("gain", money(value.subtract(basis)));
        row.put("returnPercent", round(percent(value.subtract(basis), basis)));
        return row;
    }

    private Map<String, Double> allocation(PortfolioSnapshot snapshot) {
        double total = snapshot.getTotalValue().doubleValue();
        if (total <= 0) {
            return Map.of("Stocks", 0d, "ETFs", 0d, "Bonds", 0d, "Metals", 0d);
        }
        return Map.of(
                "Stocks", round(snapshot.getStockValue().doubleValue() / total * 100),
                "ETFs", round(snapshot.getEtfValue().doubleValue() / total * 100),
                "Bonds", round(snapshot.getBondValue().doubleValue() / total * 100),
                "Metals", round(snapshot.getMetalValue().doubleValue() / total * 100));
    }

    private String historyMessage(LocalDate firstDate, LocalDate requestedStart) {
        return firstDate.isAfter(requestedStart)
                ? "Portfolio history begins " + firstDate
                        + ". Longer ranges will fill in as daily snapshots accumulate."
                : null;
    }

    private double index(BigDecimal value, BigDecimal base) {
        return base.signum() == 0 ? 100
                : round(value.divide(base, 8, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue());
    }

    private double percent(BigDecimal amount, BigDecimal base) {
        return base.signum() == 0 ? 0
                : amount.divide(base, 8, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    private double money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private record BenchmarkResult(boolean available, double returnPercent,
            NavigableMap<LocalDate, Double> prices) {
    }
}
