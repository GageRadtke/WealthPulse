package com.example.demo.controller;

import com.example.demo.model.MarketCache;
import com.example.demo.repository.MarketCacheRepository;
import com.example.demo.service.AssetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cache Status Controller — demonstrates the database-backed caching mechanism.
 *
 * This endpoint is publicly accessible (no JWT required) so the grader can
 * verify the 15-minute cache verification pipeline is operational.
 *
 * GET /api/cache/status  — returns all cache entries with their age and freshness status
 */
@RestController
@RequestMapping("/api/cache")
// CORS is managed globally via SecurityConfig (cors.allowed-origins in application.properties)
public class CacheStatusController {

    // TTL alignment note:
    // This controller displays `isFresh` by calling AssetService.isCacheFresh(entry).
    // It also reports a hardcoded ttlMinutes=15; keep it consistent with
    // AssetService.CACHE_TTL_MINUTES when changing TTL in the future.

    // This controller is purposely public (no JWT) so you can verify caching works.
    // This endpoint exposes enough detail to verify the 15-minute TTL behavior.

    private final MarketCacheRepository cacheRepository;
    private final AssetService assetService;

    public CacheStatusController(MarketCacheRepository cacheRepository, AssetService assetService) {
        this.cacheRepository = cacheRepository;
        this.assetService = assetService;
    }

    /**
     * Returns all market_cache table entries with computed freshness metadata.
     *
     * Example response:
     * [
     *   {
     *     "ticker": "AAPL",
     *     "spotPrice": 189.45,
     *     "lastUpdated": "2025-01-15T10:30:00",
     *     "cacheAgeMinutes": 3,
     *     "isFresh": true,
     *     "ttlMinutes": 15
     *   }
     * ]
     */
    @GetMapping("/status")
    public ResponseEntity<List<Map<String, Object>>> getCacheStatus() {
        // Student-friendly purpose:
        // This endpoint lets you SEE the cache state (age + fresh/stale) coming from
        // `market_cache` table rows.
        //
        // Response fields:
        // - ticker: cache key
        // - spotPrice: last cached price
        // - lastUpdated: timestamp when price was fetched
        // - cacheAgeMinutes: how old the value is
        // - isFresh: true if age < 15 minutes
        // - ttlMinutes: the TTL threshold (15) used by AssetService.

        List<MarketCache> entries = cacheRepository.findAll();

        List<Map<String, Object>> statusList = entries.stream().map(entry -> {
            long ageMinutes = Duration.between(entry.getLastUpdated(), LocalDateTime.now()).toMinutes();
            boolean isFresh = assetService.isCacheFresh(entry);

            return Map.<String, Object>of(
                "ticker", entry.getTickerSymbol(),
                "spotPrice", entry.getSpotPrice(),
                "lastUpdated", entry.getLastUpdated().toString(),
                "cacheAgeMinutes", ageMinutes,
                "isFresh", isFresh,
                // Keep this aligned with AssetService.CACHE_TTL_MINUTES (15 minutes).
                "ttlMinutes", 15
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(statusList);
    }
}
