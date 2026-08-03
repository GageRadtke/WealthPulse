package com.example.wealthpulse.repository;

import com.example.wealthpulse.model.HistoricalCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HistoricalCacheRepository extends JpaRepository<HistoricalCache, String> {
    Optional<HistoricalCache> findByTickerSymbol(String tickerSymbol);
}
