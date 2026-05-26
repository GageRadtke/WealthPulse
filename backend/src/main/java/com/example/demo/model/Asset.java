package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data; // Uses Lombok to automatically generate Getters/Setters

@Entity
@Data // Automatically adds getters, setters, and constructors
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // e.g., "Apple Stock" or "Gold"
    private String type; // e.g., "Stock" or "Metal"
    private Double quantity; // Shares or Ounces
    private Double purchasePrice;
}