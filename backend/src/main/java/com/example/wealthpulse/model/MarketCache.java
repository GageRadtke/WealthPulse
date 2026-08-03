package com.example.wealthpulse.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * MarketCache entity — implements the Cache Verification Pipeline from the ERD.
 *
 * Each row represents the last-known valid market price for a given ticker symbol.
 * The AssetService checks this table before making external API calls:
 *   - If lastUpdated is within the 15-minute TTL window → serve cached price
 *   - If stale or missing → fetch from Alpha Vantage / GoldAPI, then update this table
 *
 * Schema (market_cache table):
 *   ticker_symbol VARCHAR(20) PK, spot_price DECIMAL(19,4), last_updated TIMESTAMP
 */
@Entity
@Table(name = "market_cache")
@Data
@NoArgsConstructor
public class MarketCache {

    /** Unique market lookup key (e.g. "AAPL", "XAU", "XAG"). */
    @Id
    @Column(name = "ticker_symbol", nullable = false, length = 20)
    private String tickerSymbol;

    /** Last known valid market/spot price stored with high-precision decimal. */
    @Column(name = "spot_price", nullable = false, precision = 19, scale = 4)
    private java.math.BigDecimal spotPrice;

    /** Timestamp used to verify data freshness against the 15-minute TTL window. */
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    public MarketCache(String tickerSymbol, java.math.BigDecimal spotPrice) {
        this.tickerSymbol = tickerSymbol;
        this.spotPrice = spotPrice;
        this.lastUpdated = LocalDateTime.now();
    }
}
