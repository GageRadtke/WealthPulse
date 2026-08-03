package com.example.wealthpulse.security;

import com.example.wealthpulse.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                "wealthpulse-test-secret-that-is-long-enough-for-hmac-signing");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 60_000L);

        user = new User();
        user.setUsername("gage");
    }

    @Test
    void generatedTokenContainsUsernameAndIsValidForUser() {
        String token = jwtService.generateToken(user);

        assertEquals("gage", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));
    }

    @Test
    void tokenIsRejectedForDifferentUser() {
        String token = jwtService.generateToken(user);
        User differentUser = new User();
        differentUser.setUsername("another-user");

        assertFalse(jwtService.isTokenValid(token, differentUser));
    }

    @Test
    void expiredTokenIsRejected() throws InterruptedException {
        ReflectionTestUtils.setField(jwtService, "expirationMs", 1L);
        String token = jwtService.generateToken(user);
        Thread.sleep(10);

        assertFalse(jwtService.isTokenValid(token, user));
    }

    @Test
    void malformedTokenIsRejected() {
        assertFalse(jwtService.isTokenValid("not-a-jwt", user));
    }

    @Test
    void tokenWithModifiedSignatureIsRejected() {
        String token = jwtService.generateToken(user);
        char replacement = token.endsWith("a") ? 'b' : 'a';
        String tamperedToken = token.substring(0, token.length() - 1) + replacement;

        assertFalse(jwtService.isTokenValid(tamperedToken, user));
    }
}
