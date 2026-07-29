package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * JPA repository for User accounts.
 * Spring Data auto-generates the implementation at boot time.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Look up a user by username for login and JWT validation. */
    Optional<User> findByUsername(String username);

    /** Check for duplicate usernames during registration. */
    boolean existsByUsername(String username);

    /** Check for duplicate emails during registration. */
    boolean existsByEmail(String email);
}
