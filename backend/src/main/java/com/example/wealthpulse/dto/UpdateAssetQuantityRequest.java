package com.example.wealthpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** A signed quantity adjustment for an existing holding. */
public record UpdateAssetQuantityRequest(
        @NotNull Long id,
        @NotBlank String type,
        @NotNull BigDecimal quantity,
        BigDecimal price,
        Integer purityKarat) {
}
