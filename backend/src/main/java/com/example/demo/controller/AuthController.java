package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication Controller — public endpoints for user registration and login.
 *
 * Routes (all permitted without JWT):
 * POST /api/auth/register — create a new user account, returns JWT
 * POST /api/auth/login — validate credentials, returns JWT
 *
 * Security considerations:
 * - Passwords are BCrypt-hashed with work factor 10 before persistence.
 * - No password hash is ever returned in a response payload.
 * - Username/email uniqueness is checked before attempting to persist.
 */
@RestController
@RequestMapping("/api/auth")
// No @CrossOrigin — CORS is globally managed by SecurityConfig (config-driven)
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Authenticate an existing user and return a JWT.
     *
     * POST /api/auth/login
     * Body: { "username": "...", "password": "..." }
     *
     * Returns: 200 OK with JWT token payload
     * Returns: 401 Unauthorized if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Spring Security's AuthenticationManager validates credentials via
            // DaoAuthenticationProvider
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username().trim().toLowerCase(),
                            request.password()));

            User user = (User) auth.getPrincipal();
            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(
                    new AuthResponse(token, user.getUsername(), user.getEmail(), expirationMs));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password."));
        }
    }

    /**
     * Register a new user account.
     *
     * POST /api/auth/register
     * Body: { "username": "...", "password": "...", "email": "..." }
     *
     * Returns: 201 Created with JWT token payload
     * Returns: 409 Conflict if username or email already exists
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        String cleanUsername = request.username().trim().toLowerCase();
        String cleanEmail = request.email().trim().toLowerCase();

        // Guard against duplicate usernames (case-insensitive)
        if (userRepository.existsByUsername(cleanUsername)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Username is already taken."));
        }
        if (userRepository.existsByEmail(cleanEmail)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email is already registered."));
        }

        // Hash the password with BCrypt (work factor 10 — set in SecurityConfig)
        User user = new User();
        user.setUsername(cleanUsername);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setEmail(cleanEmail);

        userRepository.save(user);

        // Generate and return a JWT — user is immediately authenticated after
        // registration
        String token = jwtService.generateToken(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, user.getUsername(), user.getEmail(), expirationMs));
    }

    /**
     * Returns the username of the currently authenticated user (from the JWT).
     * GET /api/auth/me — requires a valid Bearer token.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(Map.of("username", user.getUsername(), "email", user.getEmail()));
    }
}
