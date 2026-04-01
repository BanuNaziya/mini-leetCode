package com.minileetcode.repository;

import com.minileetcode.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 *
 * <p>Spring Data JPA auto-implements all CRUD operations.
 * Custom query methods are derived from method names (no @Query needed).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their username (case-sensitive).
     *
     * @param username the username to search
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their email address.
     *
     * @param email the email to search
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find a user by username OR email (used for login).
     *
     * @param username the username
     * @param email    the email
     * @return Optional containing the user if found by either field
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Check whether a username is already taken.
     *
     * @param username the username to check
     * @return true if a user with this username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check whether an email is already registered.
     *
     * @param email the email to check
     * @return true if a user with this email exists
     */
    boolean existsByEmail(String email);
}
