package com.minileetcode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning per-user submission statistics.
 *
 * <p>Returned by GET /api/submissions/user/{userId}/stats
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsDto {

    private Long userId;
    private String username;

    /** Total number of submissions by this user */
    private long totalSubmissions;

    /** Number of submissions with status = ACCEPTED */
    private long acceptedSubmissions;

    /** Number of unique problems solved (at least one ACCEPTED submission) */
    private long uniqueProblemsSolved;

    /** Acceptance rate as a percentage (0–100) */
    private double acceptanceRate;

    /** Breakdown by status */
    private long wrongAnswers;
    private long timeLimitExceeded;
    private long compileErrors;
}
