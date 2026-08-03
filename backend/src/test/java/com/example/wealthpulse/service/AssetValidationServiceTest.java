package com.example.wealthpulse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.wealthpulse.model.MetalAsset;
import org.junit.jupiter.api.Test;

class AssetValidationServiceTest {

    private final AssetService marketSymbols = new AssetService(null, null, null, null) {
        @Override
        public String getMetalMarketSymbol(MetalAsset metal) {
            return metal.getTicker();
        }
    };
    private final AssetValidationService validation = new AssetValidationService(marketSymbols);

    @Test
    void goldAcceptsSupportedKaratsAndRejectsOtherValues() {
        MetalAsset gold = metal("XAU");

        assertTrue(validation.isSupportedMetalPurity(gold, 10));
        assertTrue(validation.isSupportedMetalPurity(gold, 24));
        assertFalse(validation.isSupportedMetalPurity(gold, 9));
        assertFalse(validation.isSupportedMetalPurity(gold, 925));
    }

    @Test
    void silverAcceptsSupportedFinenessAndRejectsKarats() {
        MetalAsset silver = metal("XAG");

        assertTrue(validation.isSupportedMetalPurity(silver, 925));
        assertTrue(validation.isSupportedMetalPurity(silver, 999));
        assertFalse(validation.isSupportedMetalPurity(silver, 24));
        assertFalse(validation.isSupportedMetalPurity(silver, 1000));
    }

    @Test
    void invalidPurityNormalizesToMetalSpecificDefault() {
        MetalAsset gold = metal("XAU");
        MetalAsset silver = metal("XAG");
        gold.setPurityKarat(999);
        silver.setPurityKarat(24);

        validation.normalizeMetalPurity(gold);
        validation.normalizeMetalPurity(silver);

        assertEquals(24, gold.getPurityKarat());
        assertEquals(999, silver.getPurityKarat());
    }

    private MetalAsset metal(String ticker) {
        MetalAsset metal = new MetalAsset();
        metal.setTicker(ticker);
        return metal;
    }
}
