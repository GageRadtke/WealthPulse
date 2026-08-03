package com.example.wealthpulse.controller;

import com.example.wealthpulse.service.MarketNewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// CORS is managed globally via SecurityConfig (cors.allowed-origins in application.properties)
@RestController
@RequestMapping("/api/news")
public class MarketNewsController {

    private final MarketNewsService marketNewsService;

    public MarketNewsController(MarketNewsService marketNewsService) {
        this.marketNewsService = marketNewsService;
    }

    @GetMapping("/markets")
    public ResponseEntity<?> getMarketNewsStreams() {
        try {
            var liveFeedData = marketNewsService.executeAggregation();
            return ResponseEntity.ok(liveFeedData);
        } catch (Exception err) {
            return ResponseEntity.internalServerError().body("Error gathering News Headlines: " + err.getMessage());
        }
    }
}