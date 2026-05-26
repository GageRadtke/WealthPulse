package com.example.demo.controller;

import com.example.demo.model.Asset;
import com.example.demo.repository.AssetRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "*") // Allows your React frontend to talk to this backend
public class AssetController {

    private final AssetRepository repository;

    public AssetController(AssetRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Asset> getAllAssets() {
        return repository.findAll();
    }

    @PostMapping
    public Asset createAsset(@RequestBody Asset asset) {
        return repository.save(asset);
    }
}