package com.example.wealthpulse.security;

import com.example.wealthpulse.model.User;
import com.example.wealthpulse.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * JWT Authentication Filter — executes once per request.
 *
 * Intercepts incoming requests, extracts the Bearer token from the
 * Authorization header, validates the signature via JwtService, and
 * sets the SecurityContext so Spring Security considers the user
 * authenticated for the duration of the request.
 *
 * Requests to /api/auth/** are excluded by SecurityConfig and never
 * reach this filter in a blocking way, but the filter still runs and
 * simply passes them through with no SecurityContext mutation.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Skip filter if no Bearer token is present
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Authorization header starting with 'Bearer ' present on request to {}",
                    request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // Strip the "Bearer " prefix to get the raw token
        final String jwt = authHeader.substring(7);

        try {
            final String username = jwtService.extractUsername(jwt);
            log.debug("Bearer token present; extracted username='{}' for request to {}", username,
                    request.getRequestURI());

            // Only authenticate if not already authenticated in this request
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<User> userOptional = userRepository.findByUsername(username);

                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    log.debug("Found user in DB username='{}' id={}", user.getUsername(), user.getId());

                    if (jwtService.isTokenValid(jwt, user)) {
                        // Build Spring Security authentication object and set it in context
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                user.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("JWT validated and SecurityContext set for username='{}'", user.getUsername());
                    } else {
                        log.warn("JWT present but validation failed for username='{}'", username);
                    }
                } else {
                    log.warn("No user record found for username='{}' extracted from JWT", username);
                }
            }
        } catch (Exception e) {
            // Invalid/expired token — log and continue without setting SecurityContext
            // Spring Security will enforce the 401 on protected routes
            log.warn("JWT validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
