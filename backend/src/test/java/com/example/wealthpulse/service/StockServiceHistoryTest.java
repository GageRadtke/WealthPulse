package com.example.wealthpulse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

class StockServiceHistoryTest {

    @Test
    void parsesValidFederalReservePricesAndSkipsBadRows() {
        String csv = """
                observation_date,SP500
                2026-07-31,634.25
                invalid,row
                2026-08-03,637.50
                """;

        Map<String, Double> prices = StockService.parseFredCsv(csv);

        assertEquals(Map.of("2026-07-31", 634.25, "2026-08-03", 637.50), prices);
    }
}
