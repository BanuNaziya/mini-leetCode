package com.minileetcode.service;

import com.minileetcode.dto.ProblemDto;
import com.minileetcode.model.Problem;
import com.minileetcode.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Problem management.
 *
 * <p>Handles CRUD operations, difficulty filtering, and tag search.
 * All business logic lives here to keep the controller thin.
 */
@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;

    // ----------------------------------------------------------------
    //  Read operations
    // ----------------------------------------------------------------

    /**
     * Retrieve all problems in the database.
     *
     * @return list of all problems as DTOs
     */
    public List<ProblemDto> getAllProblems() {
        return problemRepository.findAll()
                .stream()
                .map(ProblemDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a single problem by its ID.
     *
     * @param id the problem's ID
     * @return the problem DTO
     * @throws RuntimeException if no problem found with the given ID
     */
    public ProblemDto getProblemById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found with id: " + id));
        return ProblemDto.fromEntity(problem);
    }

    /**
     * Retrieve all problems matching a difficulty level.
     *
     * @param level difficulty string: "EASY", "MEDIUM", or "HARD" (case-insensitive)
     * @return list of matching problems
     * @throws IllegalArgumentException if the level string is not a valid Difficulty
     */
    public List<ProblemDto> getProblemsByDifficulty(String level) {
        // Convert string to enum - throws IllegalArgumentException on invalid value
        Problem.Difficulty difficulty;
        try {
            difficulty = Problem.Difficulty.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid difficulty level: '" + level + "'. Valid values: EASY, MEDIUM, HARD");
        }

        return problemRepository.findByDifficulty(difficulty)
                .stream()
                .map(ProblemDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Search problems by a tag keyword.
     *
     * <p>The search is case-insensitive and matches partial tag names.
     * e.g. searching "dp" will match a problem tagged "array,dp,greedy".
     *
     * @param tag the tag keyword to search
     * @return list of matching problems
     */
    public List<ProblemDto> searchByTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return getAllProblems();
        }
        return problemRepository.findByTagContaining(tag.trim())
                .stream()
                .map(ProblemDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    //  Write operations
    // ----------------------------------------------------------------

    /**
     * Create a new problem.
     *
     * @param dto the problem data from the request body
     * @return the created problem DTO with its assigned ID
     * @throws IllegalArgumentException if the difficulty value is invalid
     */
    @Transactional
    public ProblemDto createProblem(ProblemDto dto) {
        Problem problem = mapToEntity(new Problem(), dto);
        // New problems start with zero submissions and acceptance rate
        problem.setTotalSubmissions(0);
        problem.setAcceptanceRate(0.0);
        Problem saved = problemRepository.save(problem);
        return ProblemDto.fromEntity(saved);
    }

    /**
     * Update an existing problem by ID.
     *
     * @param id  the ID of the problem to update
     * @param dto updated fields
     * @return updated problem DTO
     * @throws RuntimeException if no problem found with the given ID
     */
    @Transactional
    public ProblemDto updateProblem(Long id, ProblemDto dto) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem not found with id: " + id));
        mapToEntity(problem, dto);
        Problem updated = problemRepository.save(problem);
        return ProblemDto.fromEntity(updated);
    }

    /**
     * Delete a problem by ID.
     *
     * @param id the ID of the problem to delete
     * @throws RuntimeException if no problem found with the given ID
     */
    @Transactional
    public void deleteProblem(Long id) {
        if (!problemRepository.existsById(id)) {
            throw new RuntimeException("Problem not found with id: " + id);
        }
        problemRepository.deleteById(id);
    }

    // ----------------------------------------------------------------
    //  Internal helper - called by SubmissionService
    // ----------------------------------------------------------------

    /**
     * Recalculate and persist the acceptance rate for a problem after a new submission.
     *
     * @param problemId          the problem's ID
     * @param totalSubmissions   total submission count (passed in to avoid extra query)
     * @param acceptedSubmissions number of accepted submissions
     */
    @Transactional
    public void updateAcceptanceRate(Long problemId, long totalSubmissions, long acceptedSubmissions) {
        problemRepository.findById(problemId).ifPresent(problem -> {
            problem.setTotalSubmissions((int) totalSubmissions);
            // Avoid division by zero
            double rate = totalSubmissions > 0
                    ? (acceptedSubmissions * 100.0 / totalSubmissions)
                    : 0.0;
            // Round to 1 decimal place
            problem.setAcceptanceRate(Math.round(rate * 10.0) / 10.0);
            problemRepository.save(problem);
        });
    }

    // ----------------------------------------------------------------
    //  Private helpers
    // ----------------------------------------------------------------

    /**
     * Map fields from a ProblemDto onto a Problem entity.
     *
     * @param problem the target entity (new or existing)
     * @param dto     the source DTO
     * @return the mutated entity
     */
    private Problem mapToEntity(Problem problem, ProblemDto dto) {
        if (dto.getTitle() != null)       problem.setTitle(dto.getTitle());
        if (dto.getDescription() != null) problem.setDescription(dto.getDescription());
        if (dto.getDifficulty() != null) {
            try {
                problem.setDifficulty(Problem.Difficulty.valueOf(dto.getDifficulty().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid difficulty: '" + dto.getDifficulty() + "'. Use EASY, MEDIUM, or HARD.");
            }
        }
        if (dto.getTags() != null)          problem.setTags(dto.getTags());
        if (dto.getExampleInput() != null)  problem.setExampleInput(dto.getExampleInput());
        if (dto.getExampleOutput() != null) problem.setExampleOutput(dto.getExampleOutput());
        if (dto.getConstraints() != null)   problem.setConstraints(dto.getConstraints());
        return problem;
    }
}
