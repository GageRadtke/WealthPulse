package com.example.wealthpulse.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AssetAndAuthIntegrationTest {

        @SuppressWarnings("resource")
        @Container
        static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                        .withDatabaseName("wealth_pulse_test")
                        .withUsername("wealthpulse")
                        .withPassword("wealthpulse");

        @DynamicPropertySource
        static void configureDatabase(DynamicPropertyRegistry registry) {
                registry.add("spring.datasource.url", postgres::getJdbcUrl);
                registry.add("spring.datasource.username", postgres::getUsername);
                registry.add("spring.datasource.password", postgres::getPassword);
                registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
                registry.add("jwt.secret",
                                () -> "wealthpulse-integration-secret-that-is-long-enough");
                registry.add("cors.allowed-origins", () -> "http://localhost:5173");
                registry.add("alpha.vantage.api.key", () -> "");
                registry.add("goldapi.key", () -> "");
                registry.add("wealthpulse.scheduling.enabled", () -> "false");
        }

        @Autowired
        private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Test
        void registrationAuthenticationAssetPersistenceAndTenantIsolationWorkTogether()
                        throws Exception {
                String suffix = UUID.randomUUID().toString().substring(0, 8);
                String ownerToken = register("owner-" + suffix);
                String otherToken = register("other-" + suffix);

                String assetJson = """
                                {
                                  "type": "STOCK",
                                  "ticker": "AAPL",
                                  "name": "Apple Inc.",
                                  "sector": "Technology",
                                  "assetSubType": "STOCK",
                                  "quantity": 2.5,
                                  "price": 200.00,
                                  "amountPaid": 450.00
                                }
                                """;

                String bondJson = """
                                {
                                  "type": "STOCK",
                                  "ticker": "AAPL",
                                  "name": "Apple Corporate Bond",
                                  "sector": "Corporate Debt",
                                  "assetSubType": "BOND",
                                  "bondRating": "AA+",
                                  "couponRate": 4.5,
                                  "quantity": 1.0,
                                  "price": 98.50,
                                  "amountPaid": 98.50
                                }
                                """;

                mockMvc.perform(post("/api/assets")
                                .header("Authorization", bearer(ownerToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bondJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.ticker", is("AAPL")))
                                .andExpect(jsonPath("$.assetSubType", is("BOND")))
                                .andExpect(jsonPath("$.quantity", is(1.0)));

                String createdAsset = mockMvc.perform(post("/api/assets")
                                .header("Authorization", bearer(ownerToken))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(assetJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", notNullValue()))
                                .andExpect(jsonPath("$.ticker", is("AAPL")))
                                .andExpect(jsonPath("$.assetSubType", is("STOCK")))
                                .andExpect(jsonPath("$.quantity", is(2.5)))
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                long assetId = objectMapper.readTree(createdAsset).get("id").asLong();

                mockMvc.perform(delete("/api/assets/{id}", assetId)
                                .header("Authorization", bearer(otherToken)))
                                .andExpect(status().isNotFound());

                mockMvc.perform(get("/api/assets")
                                .header("Authorization", bearer(ownerToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(2)))
                                .andExpect(jsonPath("$[?(@.assetSubType == 'STOCK')]", hasSize(1)))
                                .andExpect(jsonPath("$[?(@.assetSubType == 'BOND')]", hasSize(1)));

                mockMvc.perform(get("/api/assets")
                                .header("Authorization", bearer(otherToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(0)));

                mockMvc.perform(get("/api/assets"))
                                .andExpect(status().is(not(200)));

                mockMvc.perform(get("/api/assets")
                                .header("Authorization", "Bearer malformed-token"))
                                .andExpect(status().isUnauthorized());
        }

        private String register(String username) throws Exception {
                String body = objectMapper.writeValueAsString(new Registration(
                                username,
                                "SecureAA12!",
                                username + "@example.test"));

                String response = mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.token", notNullValue()))
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                JsonNode json = objectMapper.readTree(response);
                return json.get("token").asText();
        }

        private String bearer(String token) {
                return "Bearer " + token;
        }

        private record Registration(String username, String password, String email) {
        }
}
