package com.example.wealthpulse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.wealthpulse.service.StockFundamentals.DividendPayment;

class StockFundamentalsServiceTest {

    @Test
    void calculatesCagrFromCompletedCalendarYears() {
        LocalDate today = LocalDate.of(2026, 8, 3);
        List<DividendPayment> payments = List.of(
                new DividendPayment(LocalDate.of(2020, 3, 1), 1.00),
                new DividendPayment(LocalDate.of(2025, 3, 1), 1.20));

        Double result = StockFundamentalsService.calculateFiveYearDividendCagr(payments, today);

        assertEquals(Math.pow(1.2, 0.2) - 1, result, 0.000001);
    }

    @Test
    void returnsNullWhenFiveYearsOfHistoryAreUnavailable() {
        Double result = StockFundamentalsService.calculateFiveYearDividendCagr(
                List.of(new DividendPayment(LocalDate.of(2025, 3, 1), 1.20)),
                LocalDate.of(2026, 8, 3));

        assertNull(result);
    }
}
