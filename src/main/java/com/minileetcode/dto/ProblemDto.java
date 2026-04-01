package com.minileetcode.dto;

import com.minileetcode.model.Problem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Problem.
 *
 * <p>Used for both create/update request bodies and API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDto {

    private Long id;

    @NotBlank(message = "Problem title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @NotBlank(message = "Problem description is required")
    private String description;

    /**
     * Difficulty level as a string: "EASY", "MEDIUM", or "HARD".
     * Validated in the service layer.
     */
    @NotNull(message = "Difficulty is required")
    private String difficulty;

    /** Comma-separated tags, e.g. "array,two-pointers" */
    private String tags;

    /** Server-managed fields - ignored on create/update requests */
    private Double acceptanceRate;
    private Integer totalSubmissions;

    private String exampleInput;
    private String exampleOutput;
    private String constraints;

    /**
     * Converts a Problem entity to a ProblemDto.
     *
     * @param problem the Problem entity
     * @return populated ProblemDto
     */
    public static ProblemDto fromEntity(Problem problem) {
        ProblemDto dto = new ProblemDto();
        dto.setId(problem.getId());
        dto.setTitle(problem.getTitle());
        dto.setDescription(problem.getDescription());
        dto.setDifficulty(problem.getDifficulty() != null ? problem.getDifficulty().name() : null);
        dto.setTags(problem.getTags());
        dto.setAcceptanceRate(problem.getAcceptanceRate());
        dto.setTotalSubmissions(problem.getTotalSubmissions());
        dto.setExampleInput(problem.getExampleInput());
        dto.setExampleOutput(problem.getExampleOutput());
        dto.setConstraints(problem.getConstraints());
        return dto;
    }
}
