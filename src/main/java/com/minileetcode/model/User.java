package com.minileetcode.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User entity representing a registered user of the platform.
 *
 * <p>Roles:
 * <ul>
 *   <li>USER  - standard user, can submit solutions</li>
 *   <li>ADMIN - can create/edit/delete problems</li>
 * </ul>
 */
@Entity
@Table(
    name = "users",
    // Enforce unique constraints at the DB level
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_users_email",    columnNames = "email")
    }
)
@Data                   // Lombok: generates getters, setters, equals, hashCode, toString
@NoArgsConstructor      // Lombok: no-arg constructor required by JPA
@AllArgsConstructor     // Lombok: all-arg constructor for convenience
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 120)
    private String email;

    /** Stored as a hashed value - never store plain-text passwords */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * User role: "USER" or "ADMIN".
     * Stored as a string for readability in the database.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.USER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Running count of problems this user has solved (status = ACCEPTED) */
    @Column(name = "total_solved", nullable = false)
    private Integer totalSolved = 0;

    /**
     * Simple rank label (e.g. "Beginner", "Intermediate", "Expert").
     * Derived from totalSolved for display purposes.
     */
    @Column(length = 30)
    private String rank;

    /** Auto-populate createdAt before the entity is first persisted */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        // Assign initial rank if not already set
        if (this.rank == null || this.rank.isBlank()) {
            this.rank = "Beginner";
        }
    }

    /** Enum for user roles */
    public enum Role {
        USER, ADMIN
    }
}
