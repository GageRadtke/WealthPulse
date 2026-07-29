package com.example.demo.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@Entity
@Table(name = "assets")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StockAsset.class, name = "STOCK"),
        @JsonSubTypes.Type(value = MetalAsset.class, name = "METAL")
})
@Data
public abstract class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String ticker;

    @Column(name = "type", insertable = false, updatable = false)
    private String type;

    private Double quantity; // Shares or Ounces
    private Double price; // Current price/spot price
    private Double amountPaid;
    private LocalDateTime lastUpdated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User owner;

    public double calculateCurrentValue() {
        if (quantity == null || price == null)
            return 0.0;
        return quantity * price;
    }

    public double calculateROI() {
        if (amountPaid == null || amountPaid == 0.0)
            return 0.0;
        return ((calculateCurrentValue() - amountPaid) / amountPaid) * 100.0;
    }
}