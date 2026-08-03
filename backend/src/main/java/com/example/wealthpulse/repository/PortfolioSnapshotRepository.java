package com.example.wealthpulse.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.wealthpulse.model.PortfolioSnapshot;

public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {
    Optional<PortfolioSnapshot> findByOwnerIdAndSnapshotDate(Long ownerId, LocalDate date);
    List<PortfolioSnapshot> findByOwnerIdAndSnapshotDateBetweenOrderBySnapshotDate(
            Long ownerId, LocalDate start, LocalDate end);
}
