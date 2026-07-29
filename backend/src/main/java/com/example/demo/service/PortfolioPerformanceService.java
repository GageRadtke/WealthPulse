package com.example.demo.service;

import static com.example.demo.service.PortfolioLedgerService.decimal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.*;
import com.example.demo.repository.*;

@Service
public class PortfolioPerformanceService {
    private final AssetRepository assetRepository;
    private final AssetTransactionRepository transactionRepository;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final UserRepository userRepository;
    private final PortfolioLedgerService ledgerService;
    private final AssetService assetService;

    public PortfolioPerformanceService(AssetRepository assetRepository,
            AssetTransactionRepository transactionRepository,
            PortfolioSnapshotRepository snapshotRepository,
            UserRepository userRepository,
            PortfolioLedgerService ledgerService,
            AssetService assetService) {
        this.assetRepository = assetRepository;
        this.transactionRepository = transactionRepository;
        this.snapshotRepository = snapshotRepository;
        this.userRepository = userRepository;
        this.ledgerService = ledgerService;
        this.assetService = assetService;
    }

    @Transactional
    public Map<String, Object> performance(User owner, String requestedPeriod, String requestedBenchmark) {
        String period = normalizePeriod(requestedPeriod);
        String benchmark = requestedBenchmark == null || requestedBenchmark.isBlank()
                ? "SPY" : requestedBenchmark.trim().toUpperCase();
        LocalDate end = LocalDate.now();
        LocalDate requestedStart = startFor(period, end);

        List<Asset> assets = assetRepository.findByOwnerId(owner.getId());
        ledgerService.ensureOpeningTransactions(owner, assets);
        PortfolioSnapshot current = upsertSnapshot(owner, assets, end);
        List<PortfolioSnapshot> snapshots = snapshotRepository
                .findByOwnerIdAndSnapshotDateBetweenOrderBySnapshotDate(owner.getId(), requestedStart, end);
        if (snapshots.isEmpty()) {
            snapshots = List.of(current);
        }

        List<AssetTransaction> transactions =
                transactionRepository.findByOwnerIdOrderByTransactionDateAscIdAsc(owner.getId());
        BigDecimal currentValue = current.getTotalValue();
        BigDecimal costBasis = assets.stream().map(a -> decimal(a.getAmountPaid()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal unrealized = currentValue.subtract(costBasis);
        BigDecimal realized = realizedGain(transactions);
        BigDecimal income = sumCash(transactions, TransactionType.DIVIDEND, TransactionType.INTEREST);
        BigDecimal fees = sumCash(transactions, TransactionType.FEE);
        BigDecimal purchases = sumCash(transactions, TransactionType.BUY, TransactionType.OPENING_BALANCE);
        BigDecimal saleProceeds = sumCash(transactions, TransactionType.SELL);
        // In this manual tracker, purchases represent money added and sale proceeds
        // represent money removed. Dedicated DEPOSIT/WITHDRAWAL events can replace
        // that convention when account-level cash tracking is added.
        BigDecimal contributions = purchases.subtract(saleProceeds);
        BigDecimal growth = currentValue.subtract(contributions).add(income).subtract(fees);

        PortfolioSnapshot first = snapshots.get(0);
        double portfolioReturn = first.getTotalValue().signum() > 0
                ? percent(currentValue.subtract(first.getTotalValue()), first.getTotalValue()) : 0;
        BenchmarkResult benchmarkResult = benchmarkReturn(benchmark, first.getSnapshotDate(), end);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("period", period);
        result.put("requestedStartDate", requestedStart);
        result.put("startDate", first.getSnapshotDate());
        result.put("endDate", end);
        result.put("historyComplete", !first.getSnapshotDate().isAfter(requestedStart));
        result.put("historyMessage", first.getSnapshotDate().isAfter(requestedStart)
                ? "Portfolio history begins " + first.getSnapshotDate() + ". Longer ranges will fill in as daily snapshots accumulate."
                : null);
        result.put("benchmark", benchmark);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("currentValue", money(currentValue));
        summary.put("costBasis", money(costBasis));
        summary.put("netContributions", money(contributions));
        summary.put("investmentGrowth", money(growth));
        summary.put("realizedGain", money(realized));
        summary.put("unrealizedGain", money(unrealized));
        summary.put("income", money(income));
        summary.put("fees", money(fees));
        summary.put("portfolioReturnPercent", round(portfolioReturn));
        summary.put("benchmarkReturnPercent", round(benchmarkResult.returnPercent()));
        summary.put("benchmarkAvailable", benchmarkResult.available());
        result.put("summary", summary);
        result.put("series", series(snapshots, benchmarkResult.prices()));
        result.put("allocation", Map.of(
                "start", allocation(first),
                "end", allocation(current)));
        result.put("performers", performers(assets));
        return result;
    }

    @Transactional
    public PortfolioSnapshot upsertSnapshot(User owner, List<Asset> assets, LocalDate date) {
        PortfolioSnapshot snapshot = snapshotRepository.findByOwnerIdAndSnapshotDate(owner.getId(), date)
                .orElseGet(PortfolioSnapshot::new);
        snapshot.setOwner(owner);
        snapshot.setSnapshotDate(date);
        BigDecimal stocks = BigDecimal.ZERO;
        BigDecimal etfs = BigDecimal.ZERO;
        BigDecimal bonds = BigDecimal.ZERO;
        BigDecimal metals = BigDecimal.ZERO;
        for (Asset asset : assets) {
            BigDecimal value = decimal(asset.getQuantity()).multiply(decimal(asset.getPrice()));
            if (asset instanceof StockAsset stock) {
                String subtype = stock.getAssetSubType() == null ? "STOCK" : stock.getAssetSubType().toUpperCase();
                if ("ETF".equals(subtype)) etfs = etfs.add(value);
                else if ("BOND".equals(subtype)) bonds = bonds.add(value);
                else stocks = stocks.add(value);
            } else {
                metals = metals.add(value);
            }
        }
        snapshot.setStockValue(stocks);
        snapshot.setEtfValue(etfs);
        snapshot.setBondValue(bonds);
        snapshot.setMetalValue(metals);
        snapshot.setTotalValue(stocks.add(etfs).add(bonds).add(metals));
        snapshot.setCreatedAt(LocalDateTime.now());
        return snapshotRepository.save(snapshot);
    }

    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void captureDailySnapshots() {
        for (User user : userRepository.findAll()) {
            List<Asset> assets = assetRepository.findByOwnerId(user.getId());
            ledgerService.ensureOpeningTransactions(user, assets);
            upsertSnapshot(user, assets, LocalDate.now());
        }
    }

    private BenchmarkResult benchmarkReturn(String symbol, LocalDate start, LocalDate end) {
        try {
            NavigableMap<LocalDate, Double> prices = new TreeMap<>();
            assetService.getHistoricalWithCache(symbol).forEach((date, price) -> {
                try { prices.put(LocalDate.parse(date), price); } catch (Exception ignored) { }
            });
            Map.Entry<LocalDate, Double> first = prices.ceilingEntry(start);
            Map.Entry<LocalDate, Double> last = prices.floorEntry(end);
            boolean available = first != null && last != null && last.getKey().isAfter(first.getKey());
            double value = available ? ((last.getValue() / first.getValue()) - 1) * 100 : 0;
            return new BenchmarkResult(available, value, prices);
        } catch (Exception ex) {
            return new BenchmarkResult(false, 0, new TreeMap<>());
        }
    }

    private List<Map<String, Object>> series(List<PortfolioSnapshot> snapshots,
            NavigableMap<LocalDate, Double> benchmarkPrices) {
        List<Map<String, Object>> points = new ArrayList<>();
        BigDecimal portfolioBase = snapshots.get(0).getTotalValue();
        Map.Entry<LocalDate, Double> benchmarkBase =
                benchmarkPrices.ceilingEntry(snapshots.get(0).getSnapshotDate());
        for (PortfolioSnapshot snapshot : snapshots) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", snapshot.getSnapshotDate());
            point.put("portfolioValue", money(snapshot.getTotalValue()));
            point.put("portfolioIndex", portfolioBase.signum() > 0
                    ? round(snapshot.getTotalValue().divide(portfolioBase, 8, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue()) : 100);
            Map.Entry<LocalDate, Double> benchmarkPoint = benchmarkPrices.floorEntry(snapshot.getSnapshotDate());
            point.put("benchmarkIndex", benchmarkBase != null && benchmarkPoint != null
                    ? round(benchmarkPoint.getValue() / benchmarkBase.getValue() * 100) : null);
            points.add(point);
        }
        return points;
    }

    private Map<String, Object> performers(List<Asset> assets) {
        List<Map<String, Object>> ranked = assets.stream().filter(a -> decimal(a.getAmountPaid()).signum() > 0)
                .map(a -> {
                    BigDecimal value = decimal(a.getQuantity()).multiply(decimal(a.getPrice()));
                    BigDecimal basis = decimal(a.getAmountPaid());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("symbol", a.getTicker() == null ? a.getName() : a.getTicker());
                    row.put("name", a.getName());
                    row.put("gain", money(value.subtract(basis)));
                    row.put("returnPercent", round(percent(value.subtract(basis), basis)));
                    return row;
                })
                .sorted(Comparator.comparingDouble(r -> -((Number) r.get("returnPercent")).doubleValue()))
                .toList();
        return Map.of(
                "best", ranked.stream().limit(3).toList(),
                "worst", ranked.stream().sorted(Comparator.comparingDouble(
                        r -> ((Number) r.get("returnPercent")).doubleValue())).limit(3).toList());
    }

    private Map<String, Double> allocation(PortfolioSnapshot snapshot) {
        double total = snapshot.getTotalValue().doubleValue();
        if (total <= 0) return Map.of("Stocks", 0d, "ETFs", 0d, "Bonds", 0d, "Metals", 0d);
        return Map.of(
                "Stocks", round(snapshot.getStockValue().doubleValue() / total * 100),
                "ETFs", round(snapshot.getEtfValue().doubleValue() / total * 100),
                "Bonds", round(snapshot.getBondValue().doubleValue() / total * 100),
                "Metals", round(snapshot.getMetalValue().doubleValue() / total * 100));
    }

    private BigDecimal realizedGain(List<AssetTransaction> transactions) {
        Map<String, BigDecimal> quantities = new HashMap<>();
        Map<String, BigDecimal> bases = new HashMap<>();
        BigDecimal realized = BigDecimal.ZERO;
        for (AssetTransaction tx : transactions) {
            String key = tx.getAssetId() + ":" + tx.getSymbol();
            if (tx.getTransactionType() == TransactionType.BUY
                    || tx.getTransactionType() == TransactionType.OPENING_BALANCE) {
                quantities.merge(key, tx.getQuantity(), BigDecimal::add);
                bases.merge(key, tx.getCashAmount().add(tx.getFees()), BigDecimal::add);
            } else if (tx.getTransactionType() == TransactionType.SELL) {
                BigDecimal held = quantities.getOrDefault(key, BigDecimal.ZERO);
                BigDecimal basis = bases.getOrDefault(key, BigDecimal.ZERO);
                BigDecimal sold = tx.getQuantity().min(held);
                BigDecimal allocatedBasis = held.signum() > 0
                        ? basis.multiply(sold).divide(held, 8, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                realized = realized.add(tx.getCashAmount().subtract(tx.getFees()).subtract(allocatedBasis));
                quantities.put(key, held.subtract(sold));
                bases.put(key, basis.subtract(allocatedBasis));
            }
        }
        return realized;
    }

    private BigDecimal sumCash(List<AssetTransaction> transactions, TransactionType... types) {
        Set<TransactionType> accepted = Set.of(types);
        return transactions.stream().filter(t -> accepted.contains(t.getTransactionType()))
                .map(AssetTransaction::getCashAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String normalizePeriod(String period) {
        String normalized = period == null ? "1Y" : period.toUpperCase();
        return Set.of("1M", "3M", "1Y", "5Y", "ALL").contains(normalized) ? normalized : "1Y";
    }

    private LocalDate startFor(String period, LocalDate end) {
        return switch (period) {
            case "1M" -> end.minusMonths(1);
            case "3M" -> end.minusMonths(3);
            case "5Y" -> end.minusYears(5);
            case "ALL" -> LocalDate.of(1970, 1, 1);
            default -> end.minusYears(1);
        };
    }

    private double percent(BigDecimal numerator, BigDecimal denominator) {
        return denominator.signum() == 0 ? 0
                : numerator.divide(denominator, 8, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
    }

    private double money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private record BenchmarkResult(boolean available, double returnPercent,
            NavigableMap<LocalDate, Double> prices) { }
}
