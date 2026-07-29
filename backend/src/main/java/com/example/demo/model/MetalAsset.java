package com.example.demo.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("METAL")
@Data
@EqualsAndHashCode(callSuper = true)
public class MetalAsset extends Asset {
    private String unit;

    /**
     * Fineness used for melt valuation. Gold uses karats (10–24); silver uses
     * millesimal fineness (for example, 999 for 99.9% fine silver).
     */
    private Integer purityKarat;
}
