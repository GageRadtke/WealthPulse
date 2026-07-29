package com.example.demo.controller;

import com.example.demo.service.MarketTrackerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/market-tracker")
public class MarketTrackerController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MarketTrackerController.class);

    private final MarketTrackerService marketTrackerService;

    public MarketTrackerController(MarketTrackerService marketTrackerService) {
        this.marketTrackerService = marketTrackerService;
    }

    @GetMapping("/quotes")
    public ResponseEntity<?> getMarketQuotes() {
        try {
            return ResponseEntity.ok(marketTrackerService.getMarketQuotes());
        } catch (Exception e) {
            log.warn("Failed to load market tracker quotes", e);
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Market data temporarily unavailable", "detail", e.getMessage()));
        }
    }
}
