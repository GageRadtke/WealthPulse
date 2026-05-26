package com.example.demo.service;

import com.example.demo.model.Asset;
import com.example.demo.repository.AssetRepository;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AssetService {

    private final AssetRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${goldapi.key}") // key from application.properties
    private String goldApiKey;

    public AssetService(AssetRepository repository) {
        this.repository = repository;
        //System.out.println(">>> ASSET SERVICE INITIALIZED SUCCESSFULLY <<<");
    }

    @Scheduled(fixedRate = 144000000) // 4 hours
    public void updateAssetPrices() {
        // System.out.println(">>> TRIGGERING PRICE UPDATE... <<<");
        // Fetch all assets from DB
        List<Asset> assets = repository.findAll();

        for (Asset asset : assets) {
            if ("Metal".equalsIgnoreCase(asset.getType())) {
                updateMetalPrice(asset);
            }
        }
    }

    private void updateMetalPrice(Asset asset) {
        String url = "https://www.goldapi.io/api/XAU/USD"; // Note: Use dynamic symbols later
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-access-token", goldApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoldApiResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, GoldApiResponse.class);

            if (response.getBody() != null) {
                asset.setPurchasePrice(response.getBody().price); // Updating price
                repository.save(asset); // Saving back to DB!
                System.out.println("Updated " + asset.getName() + " to " + asset.getPurchasePrice());
            }
        } catch (Exception e) {
            System.err.println("Failed to update " + asset.getName() + ": " + e.getMessage());
        }
    }
}