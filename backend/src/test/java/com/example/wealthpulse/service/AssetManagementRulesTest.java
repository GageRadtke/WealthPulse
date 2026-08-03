package com.example.wealthpulse.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class AssetManagementRulesTest {

    @Test
    void purchaseAddsTransactionCostToCostBasis() {
        double basis = AssetManagementService.calculateAdjustedCostBasis(1_000, 10, 2, 125);

        assertEquals(1_250, basis, 0.001);
    }

    @Test
    void partialSaleRemovesProportionalCostBasis() {
        double basis = AssetManagementService.calculateAdjustedCostBasis(1_000, 10, -4, 150);

        assertEquals(600, basis, 0.001);
    }

    @Test
    void completeSaleReducesCostBasisToZero() {
        double basis = AssetManagementService.calculateAdjustedCostBasis(1_000, 10, -10, 150);

        assertEquals(0, basis, 0.001);
    }

    @Test
    void sellingMoreThanOwnedIsRejected() {
        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> AssetManagementService.rejectOversell(10, -10.01));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals("Cannot sell more than the owned quantity", error.getReason());
    }

    @Test
    void sellingExactlyTheOwnedQuantityIsAllowed() {
        AssetManagementService.rejectOversell(10, -10);
    }
}
