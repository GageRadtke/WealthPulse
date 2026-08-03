package com.example.wealthpulse.repository;

import com.example.wealthpulse.model.MarketCache;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * JPA repository for the market_cache synchronization table.
 *
 * Used by AssetService to implement the 15-minute Cache Verification Pipeline:
 * 1. Query this repository for the ticker
 * 2. If present and fresh (< 15 min) → serve cached price
 * 3. If stale or absent → fetch from external API → save here → return price
 */
public interface MarketCacheRepository extends JpaRepository<MarketCache, String> {

    Optional<MarketCache> findByTickerSymbol(String tickerSymbol);
}
