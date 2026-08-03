package com.example.wealthpulse.service;

import static com.example.wealthpulse.service.PortfolioLedgerService.decimal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.wealthpulse.model.Asset;
import com.example.wealthpulse.model.PortfolioSnapshot;
import com.example.wealthpulse.model.StockAsset;
import com.example.wealthpulse.model.User;
import com.example.wealthpulse.repository.PortfolioSnapshotRepository;

@Service
public class PortfolioSnapshotService {
    private final PortfolioSnapshotRepository snapshotRepository;

    public PortfolioSnapshotService(PortfolioSnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    @Transactional
    public PortfolioSnapshot saveDailySnapshot(User owner, List<Asset> assets, LocalDate date) {
        PortfolioSnapshot snapshot = snapshotRepository.findByOwnerIdAndSnapshotDate(owner.getId(), date)
                .orElseGet(PortfolioSnapshot::new);

        BigDecimal stocks = BigDecimal.ZERO;
        BigDecimal etfs = BigDecimal.ZERO;
        BigDecimal bonds = BigDecimal.ZERO;
        BigDecimal metals = BigDecimal.ZERO;

        for (Asset asset : assets) {
            BigDecimal value = decimal(asset.getQuantity()).multiply(decimal(asset.getPrice()));
            if (asset instanceof StockAsset stock) {
                String type = stock.getAssetSubType() == null ? "STOCK" : stock.getAssetSubType().toUpperCase();
                if ("ETF".equals(type)) {
                    etfs = etfs.add(value);
                } else if ("BOND".equals(type)) {
                    bonds = bonds.add(value);
                } else {
                    stocks = stocks.add(value);
                }
            } else {
                metals = metals.add(value);
            }
        }

        snapshot.setOwner(owner);
        snapshot.setSnapshotDate(date);
        snapshot.setStockValue(stocks);
        snapshot.setEtfValue(etfs);
        snapshot.setBondValue(bonds);
        snapshot.setMetalValue(metals);
        snapshot.setTotalValue(stocks.add(etfs).add(bonds).add(metals));
        snapshot.setCreatedAt(LocalDateTime.now());
        return snapshotRepository.save(snapshot);
    }

    public List<PortfolioSnapshot> findHistory(User owner, LocalDate start, LocalDate end,
            PortfolioSnapshot current) {
        List<PortfolioSnapshot> history = snapshotRepository
                .findByOwnerIdAndSnapshotDateBetweenOrderBySnapshotDate(owner.getId(), start, end);
        return history.isEmpty() ? List.of(current) : history;
    }
}
