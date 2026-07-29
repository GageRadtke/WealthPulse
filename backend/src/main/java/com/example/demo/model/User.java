package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * User registry entity.
 *
 * Implements Spring Security's UserDetails so it can be used directly
 * by the authentication provider without an adapter layer. Passwords
 * are stored as BCrypt hashes — plain-text is never persisted.
 *
 * Schema (users table):
 *   id BIGSERIAL PK, username VARCHAR(50) UNIQUE, password_hash VARCHAR(100),
 *   email VARCHAR(100) UNIQUE, created_at TIMESTAMP
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── UserDetails contract ────────────────────────────────────────

    /** Returns the BCrypt hash as the password for Spring Security. */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    /** No role-based permissions needed for this application. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()            { return true; }
}
