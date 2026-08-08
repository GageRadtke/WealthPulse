package com.example.wealthpulse.service;

import java.time.LocalDate;
import java.util.List;

public record StockFundamentals(
        String sector,
        Double dividendPerShare,
        Double dividendYield,
        Double payoutRatio,
        List<DividendPayment> dividends) {

    public record DividendPayment(LocalDate exDividendDate, double amount) {
    }
}
