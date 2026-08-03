package com.example.demo.service;

import com.example.demo.model.Asset;
import com.example.demo.model.MetalAsset;
import com.example.demo.model.StockAsset;
import com.example.demo.model.User;
import com.example.demo.repository.AssetRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AssetPricingService {

    private static final Logger log = LoggerFactory.getLogger(AssetPricingService.class);

    private final AssetRepository repository;
    private final AssetService assetService;
    private final AssetValidationService validationService;

    public AssetPricingService(AssetRepository repository, AssetService assetService,
            AssetValidationService validationService) {
        this.repository = repository;
        this.assetService = assetService;
        this.validationService = validationService;
    }

    public void refreshMarketPrices(List<Asset> assets) {
        Map<String, Double> metalPrices = new HashMap<>();
        Map<String, Double> storedMetalPrices = newestStoredMetalPrices(assets);
        boolean updated = false;

        for (Asset asset : assets) {
            try {
                updated |= refreshMarketPrice(asset, metalPrices, storedMetalPrices);
            } catch (RuntimeException exception) {
                log.warn("Price sync failed for asset id={}", asset.getId(), exception);
            }
        }
        if (updated) {
            repository.saveAll(assets);
        }
    }

    public void ensureMarketFallbackPrice(Asset asset) {
        if (asset.getPrice() != null && asset.getPrice() > 0) {
            cacheProvidedPrice(asset);
            return;
        }
        try {
            if (asset instanceof StockAsset stock && isMarketPriced(stock)) {
                stock.setPrice(assetService.getStockPriceWithCache(stock.getTicker().trim()));
            } else if (asset instanceof MetalAsset metal) {
                metal.setPrice(assetService.getMetalPriceWithCache(assetService.getMetalMarketSymbol(metal)));
            }
        } catch (RuntimeException exception) {
            log.warn("Unable to fetch a market price for incoming asset", exception);
        }
    }

    public void propagateMetalPrice(User owner, MetalAsset source) {
        if (owner == null || source.getPrice() == null || source.getPrice() <= 0) {
            return;
        }
        String symbol = assetService.getMetalMarketSymbol(source);
        double spotPrice = source.getPrice();
        LocalDateTime updatedAt = LocalDateTime.now();
        List<Asset> ownerAssets = repository.findByOwnerId(owner.getId());
        boolean changed = false;

        for (Asset asset : ownerAssets) {
            if (asset instanceof MetalAsset metal
                    && symbol.equals(assetService.getMetalMarketSymbol(metal))
                    && !Objects.equals(metal.getPrice(), spotPrice)) {
                metal.setPrice(spotPrice);
                metal.setLastUpdated(updatedAt);
                changed = true;
            }
        }
        assetService.writePriceToCache(symbol, spotPrice);
        if (changed) {
            repository.saveAll(ownerAssets);
        }
    }

    private boolean refreshMarketPrice(Asset asset, Map<String, Double> metalPrices,
            Map<String, Double> storedMetalPrices) {
        if (asset instanceof StockAsset stock && isMarketPriced(stock)) {
            double price = assetService.getStockPriceWithCache(stock.getTicker().trim());
            if (price > 0) {
                stock.setPrice(price);
                return true;
            }
        } else if (asset instanceof MetalAsset metal) {
            String symbol = assetService.getMetalMarketSymbol(metal);
            double price = metalPrices.computeIfAbsent(symbol,
                    key -> getMetalPrice(key, storedMetalPrices.get(key)));
            if (price > 0) {
                metal.setPrice(price);
                return true;
            }
        }
        return false;
    }

    private double getMetalPrice(String symbol, Double storedPrice) {
        try {
            return assetService.getMetalPriceWithCache(symbol);
        } catch (RuntimeException exception) {
            if (storedPrice == null) {
                throw exception;
            }
            log.warn("Using newest stored {} price because live/cache lookup failed", symbol);
            return storedPrice;
        }
    }

    private Map<String, Double> newestStoredMetalPrices(List<Asset> assets) {
        Map<String, Double> prices = new HashMap<>();
        Map<String, LocalDateTime> dates = new HashMap<>();
        for (Asset asset : assets) {
            if (asset instanceof MetalAsset metal && metal.getPrice() != null && metal.getPrice() > 0) {
                String symbol = assetService.getMetalMarketSymbol(metal);
                LocalDateTime updated = metal.getLastUpdated() == null ? LocalDateTime.MIN : metal.getLastUpdated();
                if (!dates.containsKey(symbol) || updated.isAfter(dates.get(symbol))) {
                    prices.put(symbol, metal.getPrice());
                    dates.put(symbol, updated);
                }
            }
        }
        return prices;
    }

    private void cacheProvidedPrice(Asset asset) {
        if (asset instanceof StockAsset stock && isMarketPriced(stock)) {
            assetService.writePriceToCache(stock.getTicker().trim().toUpperCase(), asset.getPrice());
        } else if (asset instanceof MetalAsset metal) {
            assetService.writePriceToCache(assetService.getMetalMarketSymbol(metal), asset.getPrice());
        }
    }

    private boolean isMarketPriced(StockAsset stock) {
        return !validationService.isBond(stock)
                && stock.getTicker() != null
                && !stock.getTicker().isBlank();
    }
}
