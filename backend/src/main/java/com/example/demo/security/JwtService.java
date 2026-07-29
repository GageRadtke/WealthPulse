package com.example.demo.security;

import com.example.demo.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Service — handles all token generation and validation logic.
 *
 * Algorithm: HMAC-SHA256 (HS256) using the configured secret key.
 * The secret and expiration are read from application.properties so
 * they can be environment-overridden without code changes.
 *
 * Token payload (claims):
 *   sub  = username
 *   iat  = issued-at timestamp
 *   exp  = expiration timestamp
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    /** Build the HMAC signing key from the configured secret string. */
    private SecretKey getSigningKey() {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (java.security.NoSuchAlgorithmException e) {
            // Fallback (should never happen for standard JVM environment with SHA-256)
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }

    /**
     * Generate a signed JWT for the given user.
     *
     * @param user  Authenticated User entity
     * @return      Compact, URL-safe JWT string
     */
    public String generateToken(User user) {
        long nowMs = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date(nowMs))
                .expiration(new Date(nowMs + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extract the username (subject claim) from a token.
     * Throws JwtException if the token is invalid or expired.
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Validate the token signature and expiry against a known user.
     *
     * @param token    JWT string from the Authorization header
     * @param user     User loaded from the database
     * @return         true if the token is valid and belongs to this user
     */
    public boolean isTokenValid(String token, User user) {
        try {
            String username = extractUsername(token);
            return username.equals(user.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    /** Parse and verify the token signature, returning the Claims body. */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
