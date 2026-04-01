package com.minileetcode.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Submission entity representing a user's code submission for a problem.
 *
 * <p>In a real judge system, code would be compiled and run in a sandbox.
 * Here the status is set by the client (or could be randomised for demo).
 */
@Entity
@Table(
    name = "submissions",
    // Index frequently queried columns for performance
    indexes = {
        @Index(name = "idx_submissions_user_id",    columnList = "user_id"),
        @Index(name = "idx_submissions_problem_id", columnList = "problem_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK to the user who submitted */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** FK to the problem being solved */
    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    /** The submitted source code - stored as TEXT */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    /**
     * Programming language of the submission.
     * Stored as a string for flexibility (Java, Python, C++, etc.)
     */
    @Column(nullable = false, length = 30)
    private String language;

    /**
     * Judge result: ACCEPTED, WRONG_ANSWER, TIME_LIMIT, COMPILE_ERROR.
     * In a real system this would be set by the judge service.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    /** Execution time in milliseconds (simulated for demo) */
    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    /** Auto-populate submittedAt before first persist */
    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }

    /** Possible statuses returned by the judge */
    public enum Status {
        ACCEPTED,
        WRONG_ANSWER,
        TIME_LIMIT,
        COMPILE_ERROR
    }
}
