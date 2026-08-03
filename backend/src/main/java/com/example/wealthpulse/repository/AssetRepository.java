package com.example.wealthpulse.repository;

import com.example.wealthpulse.model.Asset;
import com.example.wealthpulse.model.StockAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    @Query("""
            select asset
            from StockAsset asset
            where lower(asset.ticker) = lower(:ticker)
              and asset.owner.id = :ownerId
              and upper(coalesce(asset.assetSubType, 'STOCK')) = upper(:assetSubType)
            """)
    Optional<StockAsset> findStockByTickerAndSubType(
            @Param("ticker") String ticker,
            @Param("assetSubType") String assetSubType,
            @Param("ownerId") Long ownerId);

    Optional<Asset> findByNameIgnoreCaseAndOwnerId(String name, Long ownerId);

    Optional<Asset> findByIdAndOwnerId(Long id, Long ownerId);

    List<Asset> findByOwnerId(Long ownerId);
}
