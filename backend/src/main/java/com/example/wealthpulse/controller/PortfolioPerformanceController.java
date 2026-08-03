package com.example.wealthpulse.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.wealthpulse.model.User;
import com.example.wealthpulse.service.AuthenticatedUserService;
import com.example.wealthpulse.service.PortfolioPerformanceService;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioPerformanceController {
    private final PortfolioPerformanceService performanceService;
    private final AuthenticatedUserService authenticatedUserService;

    public PortfolioPerformanceController(PortfolioPerformanceService performanceService,
            AuthenticatedUserService authenticatedUserService) {
        this.performanceService = performanceService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformance(
            @RequestParam(defaultValue = "1Y") String period,
            @RequestParam(defaultValue = "SPY") String benchmark) {
        User user = authenticatedUserService.requireCurrentUser();
        return ResponseEntity.ok(performanceService.performance(user, period, benchmark));
    }
}
