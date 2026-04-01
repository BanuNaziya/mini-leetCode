package com.minileetcode.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Problem entity representing a coding challenge on the platform.
 *
 * <p>Tags are stored as a comma-separated string (e.g., "array,dp,greedy")
 * for simplicity. In a production system you would use a join table.
 */
@Entity
@Table(name = "problems")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    /** Full problem statement - stored as TEXT to allow long descriptions */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** Difficulty level: EASY, MEDIUM, or HARD */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Difficulty difficulty;

    /**
     * Comma-separated tags for categorisation (e.g., "array,sliding-window").
     * Kept simple intentionally - a real system would normalise this.
     */
    @Column(length = 300)
    private String tags;

    /** Percentage of submissions that received ACCEPTED status (0.0 - 100.0) */
    @Column(name = "acceptance_rate")
    private Double acceptanceRate = 0.0;

    /** Total number of submissions across all users for this problem */
    @Column(name = "total_submissions")
    private Integer totalSubmissions = 0;

    /** Sample input shown to users on the problem page */
    @Column(name = "example_input", columnDefinition = "TEXT")
    private String exampleInput;

    /** Expected output for the sample input */
    @Column(name = "example_output", columnDefinition = "TEXT")
    private String exampleOutput;

    /** Constraints text (e.g., "1 <= n <= 10^5") */
    @Column(columnDefinition = "TEXT")
    private String constraints;

    /** Difficulty levels for a problem */
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }
}
