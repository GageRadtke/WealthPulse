package com.example.wealthpulse.service;

import static com.example.wealthpulse.service.PortfolioLedgerService.decimal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.wealthpulse.model.Asset;
import com.example.wealthpulse.model.AssetTransaction;
import com.example.wealthpulse.model.TransactionType;

@Service
public class PortfolioMetricsService {

    public PortfolioMetrics calculate(List<Asset> assets, List<AssetTransaction> transactions,
            BigDecimal currentValue) {
        BigDecimal costBasis = assets.stream()
                .map(asset -> decimal(asset.getAmountPaid()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal purchases = sumCash(transactions, TransactionType.BUY, TransactionType.OPENING_BALANCE);
        BigDecimal sales = sumCash(transactions, TransactionType.SELL);
        BigDecimal contributions = purchases.subtract(sales);
        BigDecimal income = sumCash(transactions, TransactionType.DIVIDEND, TransactionType.INTEREST);
        BigDecimal fees = sumCash(transactions, TransactionType.FEE);
        BigDecimal growth = currentValue.subtract(contributions).add(income).subtract(fees);

        return new PortfolioMetrics(currentValue, costBasis, contributions, growth,
                realizedGain(transactions), currentValue.subtract(costBasis), income, fees);
    }

    private BigDecimal realizedGain(List<AssetTransaction> transactions) {
        Map<String, BigDecimal> quantities = new HashMap<>();
        Map<String, BigDecimal> costBases = new HashMap<>();
        BigDecimal realized = BigDecimal.ZERO;

        for (AssetTransaction transaction : transactions) {
            String key = transaction.getAssetId() + ":" + transaction.getSymbol();
            BigDecimal quantity = value(transaction.getQuantity());
            BigDecimal cash = value(transaction.getCashAmount());
            BigDecimal fees = value(transaction.getFees());

            if (isPurchase(transaction)) {
                quantities.merge(key, quantity, BigDecimal::add);
                costBases.merge(key, cash.add(fees), BigDecimal::add);
            } else if (transaction.getTransactionType() == TransactionType.SELL) {
                BigDecimal held = quantities.getOrDefault(key, BigDecimal.ZERO);
                BigDecimal basis = costBases.getOrDefault(key, BigDecimal.ZERO);
                BigDecimal sold = quantity.min(held);
                BigDecimal soldBasis = held.signum() == 0
                        ? BigDecimal.ZERO
                        : basis.multiply(sold).divide(held, 8, RoundingMode.HALF_UP);
                realized = realized.add(cash.subtract(fees).subtract(soldBasis));
                quantities.put(key, held.subtract(sold));
                costBases.put(key, basis.subtract(soldBasis));
            }
        }
        return realized;
    }

    private boolean isPurchase(AssetTransaction transaction) {
        return transaction.getTransactionType() == TransactionType.BUY
                || transaction.getTransactionType() == TransactionType.OPENING_BALANCE;
    }

    private BigDecimal sumCash(List<AssetTransaction> transactions, TransactionType... types) {
        Set<TransactionType> acceptedTypes = Set.of(types);
        return transactions.stream()
                .filter(transaction -> acceptedTypes.contains(transaction.getTransactionType()))
                .map(transaction -> value(transaction.getCashAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal value(BigDecimal value) {
        return Optional.ofNullable(value).orElse(BigDecimal.ZERO);
    }

    public record PortfolioMetrics(BigDecimal currentValue, BigDecimal costBasis,
            BigDecimal contributions, BigDecimal growth, BigDecimal realizedGain,
            BigDecimal unrealizedGain, BigDecimal income, BigDecimal fees) {
    }
}
