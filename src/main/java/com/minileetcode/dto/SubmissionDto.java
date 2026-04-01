package com.minileetcode.dto;

import com.minileetcode.model.Submission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Submission.
 *
 * <p>Used for create requests and API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDto {

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Problem ID is required")
    private Long problemId;

    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Language is required")
    private String language;

    /**
     * Status string: "ACCEPTED", "WRONG_ANSWER", "TIME_LIMIT", "COMPILE_ERROR".
     * If not provided on submit, the service assigns a simulated result.
     */
    private String status;

    /** Execution time in milliseconds (simulated) */
    private Integer executionTimeMs;

    /** Set by the server */
    private LocalDateTime submittedAt;

    // Convenience fields for the frontend (joined from other tables)
    private String username;
    private String problemTitle;

    /**
     * Converts a Submission entity to a SubmissionDto.
     *
     * @param submission the Submission entity
     * @return populated SubmissionDto
     */
    public static SubmissionDto fromEntity(Submission submission) {
        SubmissionDto dto = new SubmissionDto();
        dto.setId(submission.getId());
        dto.setUserId(submission.getUserId());
        dto.setProblemId(submission.getProblemId());
        dto.setCode(submission.getCode());
        dto.setLanguage(submission.getLanguage());
        dto.setStatus(submission.getStatus() != null ? submission.getStatus().name() : null);
        dto.setExecutionTimeMs(submission.getExecutionTimeMs());
        dto.setSubmittedAt(submission.getSubmittedAt());
        return dto;
    }
}
