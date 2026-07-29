package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.User;
import com.example.demo.service.PortfolioPerformanceService;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioPerformanceController {
    private final PortfolioPerformanceService performanceService;

    public PortfolioPerformanceController(PortfolioPerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformance(
            @RequestParam(defaultValue = "1Y") String period,
            @RequestParam(defaultValue = "SPY") String benchmark) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(performanceService.performance(user, period, benchmark));
    }
}
