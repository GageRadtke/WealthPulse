package com.example.demo.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "asset_transactions", indexes = {
        @Index(name = "idx_transaction_owner_date", columnList = "user_id,transaction_date"),
        @Index(name = "idx_transaction_asset", columnList = "asset_id")
})
@Data
public class AssetTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User owner;

    // Deliberately not a foreign key: transaction history survives a position deletion.
    @Column(name = "asset_id")
    private Long assetId;

    @Column(nullable = false)
    private String symbol;

    private String assetName;
    private String assetClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 24, scale = 8)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal pricePerUnit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cashAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fees = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String notes;
}
