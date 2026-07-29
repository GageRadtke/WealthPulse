package com.example.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Asset;
import com.example.demo.model.AssetTransaction;
import com.example.demo.model.StockAsset;
import com.example.demo.model.TransactionType;
import com.example.demo.model.User;
import com.example.demo.repository.AssetTransactionRepository;

@Service
public class PortfolioLedgerService {
    private final AssetTransactionRepository transactionRepository;

    public PortfolioLedgerService(AssetTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void ensureOpeningTransactions(User owner, List<Asset> assets) {
        for (Asset asset : assets) {
            if (asset.getId() != null
                    && !transactionRepository.existsByOwnerIdAndAssetId(owner.getId(), asset.getId())) {
                record(owner, asset, TransactionType.OPENING_BALANCE,
                        decimal(asset.getQuantity()), unitCost(asset), decimal(asset.getAmountPaid()),
                        asset.getLastUpdated() != null ? asset.getLastUpdated() : LocalDateTime.now(),
                        "Opening position imported from the pre-ledger holdings record");
            }
        }
    }

    @Transactional
    public AssetTransaction record(User owner, Asset asset, TransactionType type,
            BigDecimal quantity, BigDecimal pricePerUnit, BigDecimal cashAmount,
            LocalDateTime transactionDate, String notes) {
        AssetTransaction transaction = new AssetTransaction();
        transaction.setOwner(owner);
        transaction.setAssetId(asset.getId());
        transaction.setSymbol(asset.getTicker() == null || asset.getTicker().isBlank()
                ? asset.getName() : asset.getTicker().toUpperCase());
        transaction.setAssetName(asset.getName());
        transaction.setAssetClass(assetClass(asset));
        transaction.setTransactionType(type);
        transaction.setQuantity(quantity.abs());
        transaction.setPricePerUnit(pricePerUnit.max(BigDecimal.ZERO));
        transaction.setCashAmount(cashAmount.abs());
        transaction.setTransactionDate(transactionDate == null ? LocalDateTime.now() : transactionDate);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setNotes(notes);
        return transactionRepository.save(transaction);
    }

    public BigDecimal unitCost(Asset asset) {
        BigDecimal quantity = decimal(asset.getQuantity());
        BigDecimal basis = decimal(asset.getAmountPaid());
        return quantity.signum() > 0
                ? basis.divide(quantity, 6, RoundingMode.HALF_UP)
                : decimal(asset.getPrice());
    }

    public static BigDecimal decimal(Double value) {
        return value == null || !Double.isFinite(value) ? BigDecimal.ZERO : BigDecimal.valueOf(value);
    }

    private String assetClass(Asset asset) {
        if (asset instanceof StockAsset stock) {
            return stock.getAssetSubType() == null ? "STOCK" : stock.getAssetSubType().toUpperCase();
        }
        return "METAL";
    }
}
