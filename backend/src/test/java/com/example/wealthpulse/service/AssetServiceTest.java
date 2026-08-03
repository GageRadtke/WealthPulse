package com.example.wealthpulse.service;

import com.example.wealthpulse.model.MarketCache;
import com.example.wealthpulse.repository.AssetRepository;
import com.example.wealthpulse.repository.HistoricalCacheRepository;
import com.example.wealthpulse.repository.MarketCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetServiceTest {

    private MarketCacheRepository cacheRepository;
    private StockService stockService;
    private AssetService assetService;

    @BeforeEach
    void setUp() {
        cacheRepository = mock(MarketCacheRepository.class);
        stockService = mock(StockService.class);
        assetService = new AssetService(
                mock(AssetRepository.class),
                cacheRepository,
                mock(HistoricalCacheRepository.class),
                stockService);
    }

    @Test
    void freshCacheAvoidsExternalStockApiCall() {
        MarketCache cache = cache("AAPL", "213.45", LocalDateTime.now().minusMinutes(2));
        when(cacheRepository.findByTickerSymbol("AAPL")).thenReturn(Optional.of(cache));

        double price = assetService.getStockPriceWithCache("aapl");

        assertEquals(213.45, price, 0.001);
        verify(stockService, never()).getStockPrice("aapl");
    }

    @Test
    void staleCacheIsUsedWhenExternalStockApiFails() {
        MarketCache cache = cache("AAPL", "199.99", LocalDateTime.now().minusHours(1));
        when(cacheRepository.findByTickerSymbol("AAPL")).thenReturn(Optional.of(cache));
        when(stockService.getStockPrice("AAPL")).thenThrow(new RuntimeException("HTTP 429"));

        double price = assetService.getStockPriceWithCache("AAPL");

        assertEquals(199.99, price, 0.001);
    }

    @Test
    void missingCacheAndExternalFailureProducesClearError() {
        when(cacheRepository.findByTickerSymbol("AAPL")).thenReturn(Optional.empty());
        when(stockService.getStockPrice("AAPL")).thenThrow(new RuntimeException("network down"));

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> assetService.getStockPriceWithCache("AAPL"));

        assertEquals(
                "Stock price fetch failed for AAPL: network down",
                error.getMessage());
    }

    private MarketCache cache(String ticker, String price, LocalDateTime updated) {
        MarketCache cache = new MarketCache();
        cache.setTickerSymbol(ticker);
        cache.setSpotPrice(new BigDecimal(price));
        cache.setLastUpdated(updated);
        return cache;
    }
}
