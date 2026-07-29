package com.example.demo.controller;

import com.example.demo.model.Asset;
import com.example.demo.model.MetalAsset;
import com.example.demo.model.StockAsset;
import com.example.demo.model.User;
import com.example.demo.repository.AssetRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AssetService;
import com.example.demo.service.PortfolioLedgerService;
import com.example.demo.service.StockService;
import com.example.demo.model.TransactionType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// CORS is managed globally via SecurityConfig (cors.allowed-origins in application.properties)
@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private static final List<Integer> SUPPORTED_GOLD_PURITY_KARATS = List.of(10, 14, 18, 22, 24);
    private static final List<Integer> SUPPORTED_SILVER_PURITY_FINENESS = List.of(
            9999, 999, 958, 950, 935, 925, 900, 835, 830, 800);

    // Repository for persisting and querying asset rows.
    private final AssetRepository repository;
    private final UserRepository userRepository;
    private final AssetService assetService;
    private final StockService stockService;
    private final PortfolioLedgerService ledgerService;

    public AssetController(AssetRepository repository, UserRepository userRepository, AssetService assetService,
            StockService stockService, PortfolioLedgerService ledgerService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.assetService = assetService;
        this.stockService = stockService;
        this.ledgerService = ledgerService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AssetController.class);

    @GetMapping
    public ResponseEntity<List<Asset>> getAllAssets() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.debug("getAllAssets requested by user={} id={}", currentUser.getUsername(), currentUser.getId());
        List<Asset> assets = repository.findByOwnerId(currentUser.getId());
        refreshMarketPrices(assets);
        return ResponseEntity.ok(assets);
    }

    @PostMapping("/refresh-prices")
    @Transactional
    public ResponseEntity<List<Asset>> refreshAllPrices() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Asset> assets = repository.findByOwnerId(currentUser.getId());
        refreshMarketPrices(assets);
        return ResponseEntity.ok(assets);
    }

    private void refreshMarketPrices(List<Asset> assets) {
        Map<String, Double> metalPrices = new HashMap<>();
        Map<String, Double> storedMetalPrices = new HashMap<>();
        Map<String, LocalDateTime> storedMetalPriceDates = new HashMap<>();

        // Preserve the newest usable row price as a portfolio-level fallback.
        // This keeps sibling products in sync when the quote provider/cache is
        // temporarily unavailable.
        for (Asset asset : assets) {
            if (asset instanceof MetalAsset metal && metal.getPrice() != null && metal.getPrice() > 0) {
                String symbol = assetService.getMetalMarketSymbol(metal);
                LocalDateTime lastUpdated = metal.getLastUpdated() != null
                        ? metal.getLastUpdated()
                        : LocalDateTime.MIN;
                LocalDateTime currentNewest = storedMetalPriceDates.get(symbol);
                if (currentNewest == null || lastUpdated.isAfter(currentNewest)) {
                    storedMetalPrices.put(symbol, metal.getPrice());
                    storedMetalPriceDates.put(symbol, lastUpdated);
                }
            }
        }

        boolean updated = false;
        for (Asset asset : assets) {
            try {
                if (asset instanceof StockAsset stock
                        && !isBond(stock)
                        && stock.getTicker() != null
                        && !stock.getTicker().isBlank()) {
                    double marketPrice = assetService.getStockPriceWithCache(stock.getTicker().trim());
                    if (marketPrice > 0) {
                        stock.setPrice(marketPrice);
                        updated = true;
                    }
                } else if (asset instanceof MetalAsset metal) {
                    String symbol = assetService.getMetalMarketSymbol(metal);
                    // Every physical product in a metal market shares one lookup.
                    Double marketPrice = metalPrices.get(symbol);
                    if (marketPrice == null) {
                        try {
                            marketPrice = assetService.getMetalPriceWithCache(symbol);
                        } catch (Exception providerError) {
                            marketPrice = storedMetalPrices.get(symbol);
                            if (marketPrice == null) {
                                throw providerError;
                            }
                            log.warn("Using newest stored {} price because live/cache lookup failed", symbol);
                        }
                        metalPrices.put(symbol, marketPrice);
                    }
                    if (marketPrice > 0) {
                        metal.setPrice(marketPrice);
                        updated = true;
                    }
                }
            } catch (Exception ex) {
                log.warn("Price sync warning for asset {}: {}", asset.getName(), ex.getMessage());
            }
        }
        if (updated) {
            repository.saveAll(assets);
        }
    }

    /**
     * A metal spot quote belongs to the market symbol, not to an individual
     * physical product. Keep every product for this owner on the same quote.
     */
    private void propagateMetalPrice(User owner, MetalAsset source) {
        if (owner == null || source.getPrice() == null || source.getPrice() <= 0) {
            return;
        }

        String marketSymbol = assetService.getMetalMarketSymbol(source);
        double spotPrice = source.getPrice();
        LocalDateTime updatedAt = LocalDateTime.now();
        List<Asset> ownerAssets = repository.findByOwnerId(owner.getId());
        boolean changed = false;

        for (Asset asset : ownerAssets) {
            if (asset instanceof MetalAsset metal
                    && marketSymbol.equals(assetService.getMetalMarketSymbol(metal))
                    && !java.util.Objects.equals(metal.getPrice(), spotPrice)) {
                metal.setPrice(spotPrice);
                metal.setLastUpdated(updatedAt);
                changed = true;
            }
        }

        assetService.writePriceToCache(marketSymbol, spotPrice);
        if (changed) {
            repository.saveAll(ownerAssets);
        }
    }

    private void ensureMarketFallbackPrice(Asset asset) {
        if (asset.getPrice() != null && asset.getPrice() > 0) {
            if (asset instanceof StockAsset stock
                    && !isBond(stock)
                    && stock.getTicker() != null
                    && !stock.getTicker().isBlank()) {
                assetService.writePriceToCache(stock.getTicker().trim().toUpperCase(), asset.getPrice());
            } else if (asset instanceof MetalAsset metal) {
                assetService.writePriceToCache(assetService.getMetalMarketSymbol(metal), asset.getPrice());
            }
            return;
        }

        try {
            if (asset instanceof StockAsset stock
                    && !isBond(stock)
                    && stock.getTicker() != null
                    && !stock.getTicker().isBlank()) {
                double marketPrice = assetService.getStockPriceWithCache(stock.getTicker().trim());
                stock.setPrice(marketPrice);
            } else if (asset instanceof MetalAsset metal) {
                double marketPrice = assetService.getMetalPriceWithCache(assetService.getMetalMarketSymbol(metal));
                metal.setPrice(marketPrice);
            }
        } catch (Exception ex) {
            log.warn("Unable to fetch market price for incoming asset: {}", ex.getMessage());
        }
    }

    private boolean isSupportedMetalPurity(MetalAsset metal, Integer purity) {
        return ("XAG".equals(assetService.getMetalMarketSymbol(metal))
                ? SUPPORTED_SILVER_PURITY_FINENESS
                : SUPPORTED_GOLD_PURITY_KARATS).contains(purity);
    }

    private void normalizeMetalPurity(Asset asset) {
        if (asset instanceof MetalAsset metal && !isSupportedMetalPurity(metal, metal.getPurityKarat())) {
            metal.setPurityKarat("XAG".equals(assetService.getMetalMarketSymbol(metal)) ? 999 : 24);
        }
    }

    private String normalizeStockSubType(StockAsset stock) {
        String subType = stock.getAssetSubType();
        if (subType == null || subType.isBlank()) {
            return "STOCK";
        }
        String normalized = subType.trim().toUpperCase();
        return List.of("STOCK", "BOND", "ETF").contains(normalized)
                ? normalized
                : "STOCK";
    }

    private boolean isBond(StockAsset stock) {
        return "BOND".equals(normalizeStockSubType(stock));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Asset> createAsset(@RequestBody Asset asset) {
        // All create routes use the same merge behavior. A repeated stock
        // ticker or metal product name adds quantity and amount paid to the
        // existing position instead of creating a disconnected duplicate.
        return updateQuantityOrCreate(asset);
    }

    @PostMapping("/import/{username}")
    @Transactional
    public ResponseEntity<String> importAssetsWithUser(@PathVariable String username, @RequestBody List<Asset> assets) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<User> targetUserOpt = userRepository.findByUsername(username.trim().toLowerCase());
        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found: " + username);
        }
        User targetUser = targetUserOpt.get();

        for (Asset asset : assets) {
            asset.setLastUpdated(LocalDateTime.now());
            asset.setOwner(targetUser);
            ensureMarketFallbackPrice(asset);
        }

        repository.saveAll(assets);
        log.info("Imported {} assets for user={}({}) (requested by={})", assets.size(), targetUser.getUsername(),
                targetUser.getId(), currentUser.getUsername());
        return ResponseEntity.ok("Assets imported successfully for user: " + targetUser.getUsername());
    }

    @PostMapping("/import")
    @Transactional
    public ResponseEntity<String> importAssets(@RequestBody List<Asset> assets) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        assets.forEach(asset -> {
            asset.setLastUpdated(LocalDateTime.now());
            asset.setOwner(currentUser);
            ensureMarketFallbackPrice(asset);
        });
        repository.saveAll(assets);
        log.info("Imported {} assets for user={}({})", assets.size(), currentUser.getUsername(), currentUser.getId());
        return ResponseEntity.ok("Successfully imported " + assets.size() + " assets.");
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (repository.findByIdAndOwnerId(id, currentUser.getId()).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Asset asset = repository.findByIdAndOwnerId(id, currentUser.getId()).orElseThrow();
        ledgerService.ensureOpeningTransactions(currentUser, List.of(asset));
        ledgerService.record(currentUser, asset, TransactionType.SELL,
                PortfolioLedgerService.decimal(asset.getQuantity()),
                PortfolioLedgerService.decimal(asset.getPrice()),
                PortfolioLedgerService.decimal(asset.getQuantity())
                        .multiply(PortfolioLedgerService.decimal(asset.getPrice())),
                LocalDateTime.now(), "Position removed from holdings");
        repository.deleteById(id);
        log.info("Asset deleted id={} by user={}({})", id, currentUser.getUsername(), currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update-quantity")
    @Transactional
    public ResponseEntity<Asset> updateQuantityOrCreate(@RequestBody Asset incomingAsset) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        normalizeMetalPurity(incomingAsset);

        // An existing id represents a signed position adjustment from the
        // holdings table. Keep cost basis unchanged and never permit a
        // negative position (for example, -2 removes two shares).
        if (incomingAsset.getId() != null) {
            Optional<Asset> existingById = repository.findByIdAndOwnerId(incomingAsset.getId(), currentUser.getId());
            if (existingById.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Asset existing = existingById.get();
            ledgerService.ensureOpeningTransactions(currentUser, List.of(existing));
            double currentQty = existing.getQuantity() != null ? existing.getQuantity() : 0.0;
            double delta = incomingAsset.getQuantity() != null ? incomingAsset.getQuantity() : 0.0;
            double appliedDelta = Math.max(-currentQty, delta);
            double transactionPrice = incomingAsset.getPrice() != null && incomingAsset.getPrice() > 0
                    ? incomingAsset.getPrice()
                    : (existing.getPrice() != null ? existing.getPrice() : 0.0);
            ledgerService.record(currentUser, existing,
                    appliedDelta < 0 ? TransactionType.SELL : TransactionType.BUY,
                    java.math.BigDecimal.valueOf(Math.abs(appliedDelta)),
                    java.math.BigDecimal.valueOf(transactionPrice),
                    java.math.BigDecimal.valueOf(Math.abs(appliedDelta) * transactionPrice),
                    LocalDateTime.now(), "Quantity adjustment");
            existing.setQuantity(Math.max(0.0, currentQty + delta));
            if (appliedDelta > 0) {
                existing.setAmountPaid((existing.getAmountPaid() == null ? 0.0 : existing.getAmountPaid())
                        + appliedDelta * transactionPrice);
            } else if (appliedDelta < 0 && currentQty > 0) {
                double remainingRatio = Math.max(0.0, currentQty + appliedDelta) / currentQty;
                existing.setAmountPaid((existing.getAmountPaid() == null ? 0.0 : existing.getAmountPaid())
                        * remainingRatio);
            }
            if (existing instanceof MetalAsset existingMetal && incomingAsset instanceof MetalAsset incomingMetal) {
                existingMetal.setPurityKarat(isSupportedMetalPurity(existingMetal, incomingMetal.getPurityKarat())
                        ? incomingMetal.getPurityKarat()
                        : ("XAG".equals(assetService.getMetalMarketSymbol(existingMetal)) ? 999 : 24));
            }
            existing.setLastUpdated(LocalDateTime.now());
            return ResponseEntity.ok(repository.save(existing));
        }

        switch (incomingAsset) {
            case StockAsset incomingStock -> {
                String incomingSubType = normalizeStockSubType(incomingStock);
                incomingStock.setAssetSubType(incomingSubType);
                if (incomingStock.getTicker() != null && !incomingStock.getTicker().isEmpty()) {
                    Asset existing = repository.findStockByTickerAndSubType(
                            incomingStock.getTicker(), incomingSubType, currentUser.getId()).orElse(null);
                    if (existing instanceof StockAsset existingStock) {
                        ledgerService.ensureOpeningTransactions(currentUser, List.of(existingStock));
                        double currentQty = existingStock.getQuantity() != null ? existingStock.getQuantity() : 0.0;
                        double incomingQty = incomingStock.getQuantity() != null ? incomingStock.getQuantity() : 0.0;
                        existingStock.setQuantity(currentQty + incomingQty);

                        double currentAmountPaid = existingStock.getAmountPaid() != null ? existingStock.getAmountPaid()
                                : 0.0;
                        double incomingAmountPaid = incomingStock.getAmountPaid() != null
                                ? incomingStock.getAmountPaid()
                                : 0.0;
                        existingStock.setAmountPaid(currentAmountPaid + incomingAmountPaid);

                        if (incomingStock.getSector() != null && !incomingStock.getSector().isEmpty()) {
                            existingStock.setSector(incomingStock.getSector());
                        }

                        if (incomingStock.getPrice() != null && incomingStock.getPrice() > 0) {
                            existingStock.setPrice(incomingStock.getPrice());
                            if (!isBond(existingStock)) {
                                assetService.writePriceToCache(existingStock.getTicker().trim().toUpperCase(),
                                        incomingStock.getPrice());
                            }
                        } else if (!isBond(existingStock)) {
                            ensureMarketFallbackPrice(existingStock);
                        }
                        existingStock.setLastUpdated(LocalDateTime.now());
                        Asset saved = repository.save(existingStock);
                        ledgerService.record(currentUser, saved, TransactionType.BUY,
                                PortfolioLedgerService.decimal(incomingStock.getQuantity()),
                                ledgerService.unitCost(incomingStock),
                                PortfolioLedgerService.decimal(incomingStock.getAmountPaid()),
                                incomingStock.getLastUpdated(), "Additional purchase");
                        log.info("Asset merged (stock) id={} ticker={} by user={}({})", saved.getId(),
                                saved.getTicker(), currentUser.getUsername(), currentUser.getId());
                        return ResponseEntity.ok(saved);
                    }
                }
            }
            case MetalAsset incomingMetal -> {
                if (incomingMetal.getName() != null && !incomingMetal.getName().isEmpty()) {
                    Optional<Asset> existingAsset = repository.findByNameIgnoreCaseAndOwnerId(
                            incomingMetal.getName(), currentUser.getId());
                    if (existingAsset.isPresent() && existingAsset.get() instanceof MetalAsset existingMetal) {
                        ledgerService.ensureOpeningTransactions(currentUser, List.of(existingMetal));
                        double currentQty = existingMetal.getQuantity() != null ? existingMetal.getQuantity() : 0.0;
                        double incomingQty = incomingMetal.getQuantity() != null ? incomingMetal.getQuantity() : 0.0;
                        existingMetal.setQuantity(currentQty + incomingQty);

                        double currentAmountPaid = existingMetal.getAmountPaid() != null ? existingMetal.getAmountPaid()
                                : 0.0;
                        double incomingAmountPaid = incomingMetal.getAmountPaid() != null
                                ? incomingMetal.getAmountPaid()
                                : 0.0;
                        existingMetal.setAmountPaid(currentAmountPaid + incomingAmountPaid);

                        if (incomingMetal.getPrice() != null && incomingMetal.getPrice() > 0) {
                            existingMetal.setPrice(incomingMetal.getPrice());
                            assetService.writePriceToCache(assetService.getMetalMarketSymbol(existingMetal),
                                    incomingMetal.getPrice());
                        } else {
                            ensureMarketFallbackPrice(existingMetal);
                        }
                        existingMetal.setLastUpdated(LocalDateTime.now());
                        Asset savedMetal = repository.save(existingMetal);
                        ledgerService.record(currentUser, savedMetal, TransactionType.BUY,
                                PortfolioLedgerService.decimal(incomingMetal.getQuantity()),
                                ledgerService.unitCost(incomingMetal),
                                PortfolioLedgerService.decimal(incomingMetal.getAmountPaid()),
                                incomingMetal.getLastUpdated(), "Additional purchase");
                        propagateMetalPrice(currentUser, existingMetal);
                        log.info("Asset merged (metal) id={} name={} by user={}({})", savedMetal.getId(),
                                savedMetal.getName(), currentUser.getUsername(), currentUser.getId());
                        return ResponseEntity.ok(savedMetal);
                    }
                }
            }
            default -> {
            }
        }

        if (incomingAsset.getPrice() == null || incomingAsset.getPrice() == 0.0) {
            try {
                if (incomingAsset instanceof StockAsset incomingStock
                        && !isBond(incomingStock)
                        && incomingStock.getTicker() != null) {
                    double marketPrice = assetService.getStockPriceWithCache(incomingStock.getTicker().trim());
                    incomingStock.setPrice(marketPrice);
                } else if (incomingAsset instanceof MetalAsset incomingMetal) {
                    double marketPrice = assetService
                            .getMetalPriceWithCache(assetService.getMetalMarketSymbol(incomingMetal));
                    incomingMetal.setPrice(marketPrice);
                }
            } catch (Exception ex) {
                log.warn("Unable to fetch market price for incoming asset: {}", ex.getMessage());
            }
        }

        if (incomingAsset.getLastUpdated() == null) {
            incomingAsset.setLastUpdated(LocalDateTime.now());
        }
        incomingAsset.setOwner(currentUser);
        Asset savedIncoming = repository.save(incomingAsset);
        ledgerService.record(currentUser, savedIncoming, TransactionType.BUY,
                PortfolioLedgerService.decimal(savedIncoming.getQuantity()),
                ledgerService.unitCost(savedIncoming),
                PortfolioLedgerService.decimal(savedIncoming.getAmountPaid()),
                savedIncoming.getLastUpdated(), "Initial purchase");
        if (savedIncoming instanceof MetalAsset metal) {
            propagateMetalPrice(currentUser, metal);
        }
        log.info("Asset created (fallback) id={} name/ticker={} by user={}({})", savedIncoming.getId(),
                savedIncoming.getTicker() != null ? savedIncoming.getTicker() : savedIncoming.getName(),
                currentUser.getUsername(), currentUser.getId());
        return ResponseEntity.ok(savedIncoming);
    }

    @PutMapping("/{id}/purity")
    @Transactional
    public ResponseEntity<Asset> updateMetalPurity(@PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Asset> existingAsset = repository.findByIdAndOwnerId(id, currentUser.getId());
        if (existingAsset.isEmpty() || !(existingAsset.get() instanceof MetalAsset metal)) {
            return ResponseEntity.notFound().build();
        }

        Integer purityKarat = request.get("purityKarat");
        if (!isSupportedMetalPurity(metal, purityKarat)) {
            return ResponseEntity.badRequest().build();
        }

        metal.setPurityKarat(purityKarat);
        metal.setLastUpdated(LocalDateTime.now());
        return ResponseEntity.ok(repository.save(metal));
    }

}
