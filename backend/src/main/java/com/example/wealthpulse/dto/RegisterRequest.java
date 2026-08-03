package com.example.wealthpulse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for POST /api/auth/register */
public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 50, message = "Username cannot exceed 50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        String email
) {}
