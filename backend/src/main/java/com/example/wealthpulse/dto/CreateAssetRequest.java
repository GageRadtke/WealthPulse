package com.example.wealthpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/** Public API fields accepted when a client creates or imports an asset. */
public record CreateAssetRequest(
        @NotBlank String type,
        String name,
        String ticker,
        @NotNull @Positive BigDecimal quantity,
        @NotNull @PositiveOrZero BigDecimal amountPaid,
        @PositiveOrZero BigDecimal price,
        String sector,
        String assetSubType,
        @PositiveOrZero BigDecimal dividendYield,
        @PositiveOrZero BigDecimal payoutRatio,
        @PositiveOrZero BigDecimal cagr5Yr,
        @PositiveOrZero BigDecimal divRate,
        String bondRating,
        @PositiveOrZero BigDecimal couponRate,
        String unit,
        @Positive Integer purityKarat) {
}
