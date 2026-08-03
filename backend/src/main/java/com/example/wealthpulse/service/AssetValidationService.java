package com.example.wealthpulse.service;

import com.example.wealthpulse.model.Asset;
import com.example.wealthpulse.model.MetalAsset;
import com.example.wealthpulse.model.StockAsset;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AssetValidationService {

    private static final List<Integer> GOLD_PURITY_KARATS = List.of(10, 14, 18, 22, 24);
    private static final List<Integer> SILVER_PURITY_FINENESS = List.of(
            9999, 999, 958, 950, 935, 925, 900, 835, 830, 800);
    private static final List<String> SECURITY_SUBTYPES = List.of("STOCK", "BOND", "ETF");

    private final AssetService assetService;

    public AssetValidationService(AssetService assetService) {
        this.assetService = assetService;
    }

    public boolean isSupportedMetalPurity(MetalAsset metal, Integer purity) {
        return (isSilver(metal) ? SILVER_PURITY_FINENESS : GOLD_PURITY_KARATS).contains(purity);
    }

    public void normalizeMetalPurity(Asset asset) {
        if (asset instanceof MetalAsset metal && !isSupportedMetalPurity(metal, metal.getPurityKarat())) {
            metal.setPurityKarat(isSilver(metal) ? 999 : 24);
        }
    }

    public String normalizeStockSubType(StockAsset stock) {
        String subType = stock.getAssetSubType();
        if (subType == null || subType.isBlank()) {
            return "STOCK";
        }
        String normalized = subType.trim().toUpperCase();
        return SECURITY_SUBTYPES.contains(normalized) ? normalized : "STOCK";
    }

    public boolean isBond(StockAsset stock) {
        return "BOND".equals(normalizeStockSubType(stock));
    }

    private boolean isSilver(MetalAsset metal) {
        return "XAG".equals(assetService.getMetalMarketSymbol(metal));
    }
}
