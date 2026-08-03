package com.example.wealthpulse.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.wealthpulse.model.Asset;
import com.example.wealthpulse.model.PortfolioSnapshot;
import com.example.wealthpulse.model.User;
import com.example.wealthpulse.repository.AssetRepository;
import com.example.wealthpulse.repository.AssetTransactionRepository;
import com.example.wealthpulse.repository.UserRepository;

@Service
public class PortfolioPerformanceService {
    private static final String DEFAULT_PERIOD = "1Y";
    private static final String DEFAULT_BENCHMARK = "SPY";
    private static final Set<String> VALID_PERIODS = Set.of("1M", "3M", "1Y", "5Y", "ALL");

    private final AssetRepository assetRepository;
    private final AssetTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PortfolioLedgerService ledgerService;
    private final PortfolioSnapshotService snapshotService;
    private final PortfolioMetricsService metricsService;
    private final PortfolioReportService reportService;

    public PortfolioPerformanceService(AssetRepository assetRepository,
            AssetTransactionRepository transactionRepository, UserRepository userRepository,
            PortfolioLedgerService ledgerService, PortfolioSnapshotService snapshotService,
            PortfolioMetricsService metricsService, PortfolioReportService reportService) {
        this.assetRepository = assetRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.ledgerService = ledgerService;
        this.snapshotService = snapshotService;
        this.metricsService = metricsService;
        this.reportService = reportService;
    }

    @Transactional
    public Map<String, Object> performance(User owner, String requestedPeriod, String requestedBenchmark) {
        String period = normalizePeriod(requestedPeriod);
        String benchmark = normalizeBenchmark(requestedBenchmark);
        LocalDate end = LocalDate.now();
        LocalDate start = startDate(period, end);

        List<Asset> assets = assetRepository.findByOwnerId(owner.getId());
        ledgerService.ensureOpeningTransactions(owner, assets);

        PortfolioSnapshot current = snapshotService.saveDailySnapshot(owner, assets, end);
        List<PortfolioSnapshot> history = snapshotService.findHistory(owner, start, end, current);
        var transactions = transactionRepository.findByOwnerIdOrderByTransactionDateAscIdAsc(owner.getId());
        var metrics = metricsService.calculate(assets, transactions, current.getTotalValue());

        return reportService.build(period, benchmark, start, end, history, current, assets, metrics);
    }

    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void captureDailySnapshots() {
        LocalDate today = LocalDate.now();
        for (User user : userRepository.findAll()) {
            List<Asset> assets = assetRepository.findByOwnerId(user.getId());
            ledgerService.ensureOpeningTransactions(user, assets);
            snapshotService.saveDailySnapshot(user, assets, today);
        }
    }

    private String normalizePeriod(String requestedPeriod) {
        String period = requestedPeriod == null ? DEFAULT_PERIOD : requestedPeriod.trim().toUpperCase();
        return VALID_PERIODS.contains(period) ? period : DEFAULT_PERIOD;
    }

    private String normalizeBenchmark(String requestedBenchmark) {
        return requestedBenchmark == null || requestedBenchmark.isBlank()
                ? DEFAULT_BENCHMARK
                : requestedBenchmark.trim().toUpperCase();
    }

    private LocalDate startDate(String period, LocalDate end) {
        return switch (period) {
            case "1M" -> end.minusMonths(1);
            case "3M" -> end.minusMonths(3);
            case "5Y" -> end.minusYears(5);
            case "ALL" -> LocalDate.of(1970, 1, 1);
            default -> end.minusYears(1);
        };
    }
}
