package com.example.wealthpulse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.wealthpulse.model.AssetTransaction;

public interface AssetTransactionRepository extends JpaRepository<AssetTransaction, Long> {
    List<AssetTransaction> findByOwnerIdOrderByTransactionDateAscIdAsc(Long ownerId);
    boolean existsByOwnerIdAndAssetId(Long ownerId, Long assetId);
}
