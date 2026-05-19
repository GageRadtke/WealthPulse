package com.example.wealthpulse.repository;

import com.example.wealthpulse.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    // Spring now automatically provides methods like save(), findAll(), and deleteById()
}