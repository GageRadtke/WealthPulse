package com.example.wealthpulse.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("STOCK")
@Data
@EqualsAndHashCode(callSuper = true)
public class StockAsset extends Asset {
    private String sector;
    private Double dividendYield;
    private Double payoutRatio;
    private Double cagr5Yr;
    private Double divRate;
    /**
     * Asset sub-classification.
     * Values: "STOCK" (default), "BOND", "ETF"
     * Drives conditional display of bond-specific columns in the AssetTable.
     */
    private String assetSubType;

    /**
     * Bond credit rating — e.g. "AAA", "AA-", "BBB+", "BB".
     * Only populated when assetSubType = "BOND".
     */
    private String bondRating;

    /**
     * Annual coupon rate as a percentage — e.g. 4.5 for 4.50%.
     * Only populated when assetSubType = "BOND".
     */
    private Double couponRate;
}
