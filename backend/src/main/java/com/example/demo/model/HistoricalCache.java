package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "historical_cache")
@Data
@NoArgsConstructor
public class HistoricalCache {

    @Id
    @Column(name = "ticker_symbol", nullable = false, length = 20)
    private String tickerSymbol;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload; // JSON serialized date->price map

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    public HistoricalCache(String tickerSymbol, String payload) {
        this.tickerSymbol = tickerSymbol;
        this.payload = payload;
        this.lastUpdated = LocalDateTime.now();
    }
}
