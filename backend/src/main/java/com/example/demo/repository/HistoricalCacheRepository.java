package com.example.demo.repository;

import com.example.demo.model.HistoricalCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HistoricalCacheRepository extends JpaRepository<HistoricalCache, String> {
    Optional<HistoricalCache> findByTickerSymbol(String tickerSymbol);
}
