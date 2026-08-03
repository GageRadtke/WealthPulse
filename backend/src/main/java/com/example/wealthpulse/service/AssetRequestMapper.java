package com.example.wealthpulse.service;

import com.example.wealthpulse.dto.CreateAssetRequest;
import com.example.wealthpulse.dto.UpdateAssetQuantityRequest;
import com.example.wealthpulse.model.Asset;
import com.example.wealthpulse.model.MetalAsset;
import com.example.wealthpulse.model.StockAsset;
import java.math.BigDecimal;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AssetRequestMapper {

    public Asset toAsset(CreateAssetRequest request) {
        Asset asset = switch (request.type().trim().toUpperCase(Locale.ROOT)) {
            case "STOCK" -> toStockAsset(request);
            case "METAL" -> toMetalAsset(request);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported asset type");
        };

        asset.setName(request.name());
        asset.setTicker(request.ticker());
        asset.setQuantity(value(request.quantity()));
        asset.setAmountPaid(value(request.amountPaid()));
        asset.setPrice(value(request.price()));
        return asset;
    }

    public Asset toAsset(UpdateAssetQuantityRequest request) {
        Asset asset = switch (request.type().trim().toUpperCase(Locale.ROOT)) {
            case "STOCK" -> new StockAsset();
            case "METAL" -> new MetalAsset();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported asset type");
        };
        asset.setId(request.id());
        asset.setQuantity(value(request.quantity()));
        asset.setPrice(value(request.price()));
        if (asset instanceof MetalAsset metal) {
            metal.setPurityKarat(request.purityKarat());
        }
        return asset;
    }

    private StockAsset toStockAsset(CreateAssetRequest request) {
        StockAsset stock = new StockAsset();
        stock.setSector(request.sector());
        stock.setAssetSubType(request.assetSubType());
        stock.setDividendYield(value(request.dividendYield()));
        stock.setPayoutRatio(value(request.payoutRatio()));
        stock.setCagr5Yr(value(request.cagr5Yr()));
        stock.setDivRate(value(request.divRate()));
        stock.setBondRating(request.bondRating());
        stock.setCouponRate(value(request.couponRate()));
        return stock;
    }

    private MetalAsset toMetalAsset(CreateAssetRequest request) {
        MetalAsset metal = new MetalAsset();
        metal.setUnit(request.unit());
        metal.setPurityKarat(request.purityKarat());
        return metal;
    }

    private static Double value(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
