package com.example.wealthpulse.controller;

import com.example.wealthpulse.dto.CreateAssetRequest;
import com.example.wealthpulse.dto.UpdateAssetQuantityRequest;
import com.example.wealthpulse.dto.UpdateMetalPurityRequest;
import com.example.wealthpulse.model.Asset;
import com.example.wealthpulse.model.User;
import com.example.wealthpulse.service.AssetManagementService;
import com.example.wealthpulse.service.AssetRequestMapper;
import com.example.wealthpulse.service.AuthenticatedUserService;
import com.example.wealthpulse.service.StockFundamentalsService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AuthenticatedUserService authenticatedUserService;
    private final AssetManagementService assetManagementService;
    private final AssetRequestMapper assetRequestMapper;
    private final StockFundamentalsService stockFundamentalsService;

    public AssetController(AuthenticatedUserService authenticatedUserService,
            AssetManagementService assetManagementService,
            AssetRequestMapper assetRequestMapper,
            StockFundamentalsService stockFundamentalsService) {
        this.authenticatedUserService = authenticatedUserService;
        this.assetManagementService = assetManagementService;
        this.assetRequestMapper = assetRequestMapper;
        this.stockFundamentalsService = stockFundamentalsService;
    }

    @GetMapping
    public ResponseEntity<List<Asset>> getAllAssets() {
        User user = authenticatedUserService.requireCurrentUser();
        return ResponseEntity.ok(assetManagementService.getAllAssets(user));
    }

    @PostMapping("/refresh-prices")
    public ResponseEntity<List<Asset>> refreshAllPrices() {
        User user = authenticatedUserService.requireCurrentUser();
        return ResponseEntity.ok(assetManagementService.getAllAssets(user));
    }

    @PostMapping("/refresh-fundamentals")
    public ResponseEntity<StockFundamentalsService.RefreshResult> refreshFundamentals(
            @RequestParam(defaultValue = "false") boolean force) {
        User user = authenticatedUserService.requireCurrentUser();
        return ResponseEntity.ok(stockFundamentalsService.refreshForUser(user, force));
    }

    @PostMapping
    public ResponseEntity<Asset> createAsset(@Valid @RequestBody CreateAssetRequest request) {
        User user = authenticatedUserService.requireCurrentUser();
        return ResponseEntity.ok(assetManagementService.updateQuantityOrCreate(user,
                assetRequestMapper.toAsset(request)));
    }

    @PostMapping("/import")
    public ResponseEntity<String> importAssets(
            @Valid @RequestBody List<@Valid CreateAssetRequest> requests) {
        User user = authenticatedUserService.requireCurrentUser();
        List<Asset> assets = requests.stream().map(assetRequestMapper::toAsset).toList();
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
    public ResponseEntity<Asset> updateQuantity(
            @Valid @RequestBody UpdateAssetQuantityRequest request) {
        User user = authenticatedUserService.requireCurrentUser();
        return ResponseEntity.ok(assetManagementService.updateQuantityOrCreate(user,
                assetRequestMapper.toAsset(request)));
    }

    @PutMapping("/{id}/purity")
    public ResponseEntity<Asset> updateMetalPurity(@PathVariable Long id,
            @Valid @RequestBody UpdateMetalPurityRequest request) {
        User user = authenticatedUserService.requireCurrentUser();
        return ResponseEntity.ok(
                assetManagementService.updateMetalPurity(user, id, request.purityKarat()));
    }
}
