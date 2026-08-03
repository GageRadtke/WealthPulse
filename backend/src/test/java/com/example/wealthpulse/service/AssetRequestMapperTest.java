package com.example.wealthpulse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.wealthpulse.dto.CreateAssetRequest;
import com.example.wealthpulse.dto.UpdateAssetQuantityRequest;
import com.example.wealthpulse.model.Asset;
import com.example.wealthpulse.model.MetalAsset;
import com.example.wealthpulse.model.StockAsset;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AssetRequestMapperTest {

    private final AssetRequestMapper mapper = new AssetRequestMapper();

    @Test
    void mapsStockCreateFieldsWithoutPersistenceState() {
        CreateAssetRequest request = new CreateAssetRequest(
                "stock", "Apple", "AAPL", new BigDecimal("2.5"),
                new BigDecimal("450"), new BigDecimal("200"), "Technology",
                "STOCK", null, null, null, null, null, null, null, null);

        Asset mapped = mapper.toAsset(request);

        StockAsset stock = assertInstanceOf(StockAsset.class, mapped);
        assertEquals("AAPL", stock.getTicker());
        assertEquals(2.5, stock.getQuantity());
        assertNull(stock.getId());
        assertNull(stock.getOwner());
        assertNull(stock.getLastUpdated());
    }

    @Test
    void mapsSignedMetalQuantityAdjustment() {
        UpdateAssetQuantityRequest request = new UpdateAssetQuantityRequest(
                42L, "METAL", new BigDecimal("-1.25"), null, 925);

        MetalAsset metal = assertInstanceOf(MetalAsset.class, mapper.toAsset(request));

        assertEquals(42L, metal.getId());
        assertEquals(-1.25, metal.getQuantity());
        assertEquals(925, metal.getPurityKarat());
    }
}
