package com.example.demo.controller;

import com.example.demo.model.Asset;
import com.example.demo.model.User;
import com.example.demo.service.AssetManagementService;
import com.example.demo.service.AuthenticatedUserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AuthenticatedUserService authenticatedUserService;
    private final AssetManagementService assetManagementService;

    public AssetController(AuthenticatedUserService authenticatedUserService,
            AssetManagementService assetManagementService) {
        this.authenticatedUserService = authenticatedUserService;
        this.assetManagementService = assetManagementService;
    }

    @GetMapping
    public ResponseEntity<List<Asset>> getAllAssets() {
        User user = authenticatedUserService.requireCurrentUser();
        return ResponseEntity.ok(assetManagementService.getAllAssets(user));
    }

    @PostMapping("/refresh-prices")
    public ResponseEntity<List<Asset>> refreshAllPrices() {
        User user = authenticatedUserService.requireCurrentUser();
        return ResponseEntity.ok(assetManagementService.refreshAllPrices(user));
    }

    @PostMapping
    public ResponseEntity<Asset> createAsset(@Valid @RequestBody Asset asset) {
        User user = authenticatedUserService.requireCurrentUser();
        return ResponseEntity.ok(assetManagementService.updateQuantityOrCreate(user, asset));
    }

    @PostMapping("/import")
    public ResponseEntity<String> importAssets(@Valid @RequestBody List<Asset> assets) {
        User user = authenticatedUserService.requireCurrentUser();
        int count = assetManagementService.importAssets(user, assets);
        return ResponseEntity.ok("Successfully imported " + count + " assets.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        User user = authenticatedUserService.requireCurrentUser();
        assetManagementService.deleteAsset(user, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update-quantity")
    public ResponseEntity<Asset> updateQuantityOrCreate(@Valid @RequestBody Asset asset) {
        User user = authenticatedUserService.requireCurrentUser();
        return ResponseEntity.ok(assetManagementService.updateQuantityOrCreate(user, asset));
    }

    @PutMapping("/{id}/purity")
    public ResponseEntity<Asset> updateMetalPurity(@PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        User user = authenticatedUserService.requireCurrentUser();
        return ResponseEntity.ok(assetManagementService.updateMetalPurity(user, id, request.get("purityKarat")));
    }
}
