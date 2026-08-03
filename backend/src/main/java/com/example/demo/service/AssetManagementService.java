package com.example.demo.service;

import com.example.demo.model.Asset;
import com.example.demo.model.MetalAsset;
import com.example.demo.model.StockAsset;
import com.example.demo.model.TransactionType;
import com.example.demo.model.User;
import com.example.demo.repository.AssetRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AssetManagementService {

    private static final Logger log = LoggerFactory.getLogger(AssetManagementService.class);

    private final AssetRepository repository;
    private final AssetPricingService pricingService;
    private final AssetValidationService validationService;
    private final PortfolioLedgerService ledgerService;

    public AssetManagementService(AssetRepository repository, AssetPricingService pricingService,
            AssetValidationService validationService, PortfolioLedgerService ledgerService) {
        this.repository = repository;
        this.pricingService = pricingService;
        this.validationService = validationService;
        this.ledgerService = ledgerService;
    }

    @Transactional
    public List<Asset> getAllAssets(User user) {
        List<Asset> assets = repository.findByOwnerId(user.getId());
        pricingService.refreshMarketPrices(assets);
        return assets;
    }

    @Transactional
    public List<Asset> refreshAllPrices(User user) {
        return getAllAssets(user);
    }

    @Transactional
    public Asset updateQuantityOrCreate(User user, Asset incomingAsset) {
        validationService.normalizeMetalPurity(incomingAsset);
        if (incomingAsset.getId() != null) {
            return adjustExistingQuantity(user, incomingAsset);
        }
        if (incomingAsset instanceof StockAsset stock) {
            Asset merged = mergeStock(user, stock);
            if (merged != null) {
                return merged;
            }
        } else if (incomingAsset instanceof MetalAsset metal) {
            Asset merged = mergeMetal(user, metal);
            if (merged != null) {
                return merged;
            }
        }
        return createAsset(user, incomingAsset);
    }

    @Transactional
    public int importAssets(User user, List<Asset> assets) {
        for (Asset asset : assets) {
            asset.setId(null);
            asset.setOwner(user);
            asset.setLastUpdated(LocalDateTime.now());
            validationService.normalizeMetalPurity(asset);
            pricingService.ensureMarketFallbackPrice(asset);
        }
        repository.saveAll(assets);
        log.info("Imported {} assets for user={}({})", assets.size(), user.getUsername(), user.getId());
        return assets.size();
    }

    @Transactional
    public void deleteAsset(User user, Long id) {
        Asset asset = findOwnedAsset(user, id);
        ledgerService.ensureOpeningTransactions(user, List.of(asset));
        ledgerService.record(user, asset, TransactionType.SELL,
                decimal(asset.getQuantity()), decimal(asset.getPrice()),
                decimal(asset.getQuantity()).multiply(decimal(asset.getPrice())),
                LocalDateTime.now(), "Position removed from holdings");
        repository.delete(asset);
        log.info("Asset deleted id={} by user={}({})", id, user.getUsername(), user.getId());
    }

    @Transactional
    public Asset updateMetalPurity(User user, Long id, Integer purity) {
        Asset asset = findOwnedAsset(user, id);
        if (!(asset instanceof MetalAsset metal)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Metal asset not found");
        }
        if (!validationService.isSupportedMetalPurity(metal, purity)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported metal purity");
        }
        metal.setPurityKarat(purity);
        metal.setLastUpdated(LocalDateTime.now());
        return repository.save(metal);
    }

    private Asset adjustExistingQuantity(User user, Asset incoming) {
        Asset existing = findOwnedAsset(user, incoming.getId());
        ledgerService.ensureOpeningTransactions(user, List.of(existing));
        double currentQuantity = value(existing.getQuantity());
        double requestedDelta = value(incoming.getQuantity());
        double appliedDelta = Math.max(-currentQuantity, requestedDelta);
        double transactionPrice = positive(incoming.getPrice()) ? incoming.getPrice() : value(existing.getPrice());

        ledgerService.record(user, existing,
                appliedDelta < 0 ? TransactionType.SELL : TransactionType.BUY,
                BigDecimal.valueOf(Math.abs(appliedDelta)), BigDecimal.valueOf(transactionPrice),
                BigDecimal.valueOf(Math.abs(appliedDelta) * transactionPrice),
                LocalDateTime.now(), "Quantity adjustment");

        existing.setQuantity(Math.max(0.0, currentQuantity + requestedDelta));
        updateCostBasis(existing, currentQuantity, appliedDelta, transactionPrice);
        updatePurity(existing, incoming);
        existing.setLastUpdated(LocalDateTime.now());
        return repository.save(existing);
    }

    private Asset mergeStock(User user, StockAsset incoming) {
        String subType = validationService.normalizeStockSubType(incoming);
        incoming.setAssetSubType(subType);
        if (incoming.getTicker() == null || incoming.getTicker().isBlank()) {
            return null;
        }
        StockAsset existing = repository.findStockByTickerAndSubType(incoming.getTicker(), subType, user.getId())
                .orElse(null);
        if (existing == null) {
            return null;
        }

        ledgerService.ensureOpeningTransactions(user, List.of(existing));
        addPosition(existing, incoming);
        if (incoming.getSector() != null && !incoming.getSector().isBlank()) {
            existing.setSector(incoming.getSector());
        }
        if (positive(incoming.getPrice())) {
            existing.setPrice(incoming.getPrice());
            pricingService.ensureMarketFallbackPrice(existing);
        } else if (!validationService.isBond(existing)) {
            pricingService.ensureMarketFallbackPrice(existing);
        }
        existing.setLastUpdated(LocalDateTime.now());
        Asset saved = repository.save(existing);
        recordPurchase(user, saved, incoming, "Additional purchase");
        log.info("Asset merged (stock) id={} ticker={} by user={}({})",
                saved.getId(), saved.getTicker(), user.getUsername(), user.getId());
        return saved;
    }

    private Asset mergeMetal(User user, MetalAsset incoming) {
        if (incoming.getName() == null || incoming.getName().isBlank()) {
            return null;
        }
        Asset match = repository.findByNameIgnoreCaseAndOwnerId(incoming.getName(), user.getId()).orElse(null);
        if (!(match instanceof MetalAsset existing)) {
            return null;
        }

        ledgerService.ensureOpeningTransactions(user, List.of(existing));
        addPosition(existing, incoming);
        if (positive(incoming.getPrice())) {
            existing.setPrice(incoming.getPrice());
        }
        pricingService.ensureMarketFallbackPrice(existing);
        existing.setLastUpdated(LocalDateTime.now());
        Asset saved = repository.save(existing);
        recordPurchase(user, saved, incoming, "Additional purchase");
        pricingService.propagateMetalPrice(user, existing);
        log.info("Asset merged (metal) id={} name={} by user={}({})",
                saved.getId(), saved.getName(), user.getUsername(), user.getId());
        return saved;
    }

    private Asset createAsset(User user, Asset incoming) {
        pricingService.ensureMarketFallbackPrice(incoming);
        if (incoming.getLastUpdated() == null) {
            incoming.setLastUpdated(LocalDateTime.now());
        }
        incoming.setOwner(user);
        Asset saved = repository.save(incoming);
        recordPurchase(user, saved, saved, "Initial purchase");
        if (saved instanceof MetalAsset metal) {
            pricingService.propagateMetalPrice(user, metal);
        }
        log.info("Asset created id={} name/ticker={} by user={}({})", saved.getId(),
                saved.getTicker() != null ? saved.getTicker() : saved.getName(),
                user.getUsername(), user.getId());
        return saved;
    }

    private void addPosition(Asset existing, Asset incoming) {
        existing.setQuantity(value(existing.getQuantity()) + value(incoming.getQuantity()));
        existing.setAmountPaid(value(existing.getAmountPaid()) + value(incoming.getAmountPaid()));
    }

    private void updateCostBasis(Asset existing, double currentQuantity, double delta, double price) {
        double amountPaid = value(existing.getAmountPaid());
        if (delta > 0) {
            existing.setAmountPaid(amountPaid + delta * price);
        } else if (delta < 0 && currentQuantity > 0) {
            existing.setAmountPaid(amountPaid * Math.max(0.0, currentQuantity + delta) / currentQuantity);
        }
    }

    private void updatePurity(Asset existing, Asset incoming) {
        if (existing instanceof MetalAsset metal && incoming instanceof MetalAsset incomingMetal) {
            Integer purity = incomingMetal.getPurityKarat();
            if (validationService.isSupportedMetalPurity(metal, purity)) {
                metal.setPurityKarat(purity);
            } else {
                validationService.normalizeMetalPurity(metal);
            }
        }
    }

    private void recordPurchase(User user, Asset saved, Asset incoming, String note) {
        ledgerService.record(user, saved, TransactionType.BUY,
                decimal(incoming.getQuantity()), ledgerService.unitCost(incoming),
                decimal(incoming.getAmountPaid()), incoming.getLastUpdated(), note);
    }

    private Asset findOwnedAsset(User user, Long id) {
        return repository.findByIdAndOwnerId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"));
    }

    private static boolean positive(Double value) {
        return value != null && value > 0;
    }

    private static double value(Double value) {
        return value == null ? 0.0 : value;
    }

    private static BigDecimal decimal(Double value) {
        return PortfolioLedgerService.decimal(value);
    }
}
