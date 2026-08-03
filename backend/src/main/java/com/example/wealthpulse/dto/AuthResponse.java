package com.example.wealthpulse.dto;

/** JWT token response payload — returned on successful login or register. */
public record AuthResponse(
        String token,
        String username,
        String email,
        long expiresInMs
) {}
