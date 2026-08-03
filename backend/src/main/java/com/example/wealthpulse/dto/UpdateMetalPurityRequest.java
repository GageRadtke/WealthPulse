package com.example.wealthpulse.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Gold uses karats while silver uses millesimal fineness, so validation is metal-specific. */
public record UpdateMetalPurityRequest(
        @NotNull @Positive Integer purityKarat) {
}
