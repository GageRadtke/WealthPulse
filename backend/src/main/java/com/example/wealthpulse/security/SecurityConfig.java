package com.example.wealthpulse.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.http.HttpMethod;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration — stateless JWT-based protection.
 *
 * Design principles:
 * - CORS is configured here via application.properties (cors.allowed-origins),
 * replacing all hardcoded @CrossOrigin annotations on controllers.
 * - Session management is STATELESS; no server-side session is ever created.
 * - /api/auth/** is fully public (login + register endpoints).
 * - All other /api/** routes require a valid Bearer JWT token.
 * - BCrypt with work factor 10 is used for all password hashing.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Config-driven CORS origin — set via CORS_ALLOWED_ORIGINS env var or
     * application.properties.
     */
    @Value("${cors.allowed-origins}")
    private String allowedOrigin;

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF not needed for stateless REST APIs
                .csrf(csrf -> csrf.disable())

                // CORS using the config-driven bean defined below
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Route-level authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Public: login + register
                        .requestMatchers(HttpMethod.GET, "/api/users/profile-picture/**").permitAll() // Public profile
                                                                                                      // pics
                        .requestMatchers("/api/cache/status").permitAll()
                        .requestMatchers("/error").permitAll()
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                        .anyRequest().authenticated() // Everything else requires JWT
                )

                // Stateless — no HTTP session storage
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Return 401 for invalid/missing authentication instead of 403
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> response
                        .sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")))

                // Wire in the JWT filter before Spring's default username/password filter
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS configuration source — reads allowed origins from
     * application.properties.
     * A single source of truth replaces all @CrossOrigin annotations.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // CORS rules come from a comma-separated config value `cors.allowed-origins`.
        // Example property:
        // cors.allowed-origins=http://localhost:5173,http://127.0.0.1:5173
        //
        // If your frontend uses a different port/host, update that env var/property.

        CorsConfiguration config = new CorsConfiguration();
        // Split by comma into multiple allowed origins.
        // (If the property contains spaces, they should be trimmed.)
        // Example property:
        // cors.allowed-origins=http://localhost:5173, http://127.0.0.1:5173
        List<String> origins = List.of(allowedOrigin.split(","))
                .stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isEmpty())
                .toList();

        // If origins are not configured, fall back to an empty list (no origins
        // allowed)
        // rather than accidentally allowing invalid/blank entries.
        config.setAllowedOrigins(origins);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * BCryptPasswordEncoder with work factor 10 — matches the design paper's
     * specification for BCrypt key derivation.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
