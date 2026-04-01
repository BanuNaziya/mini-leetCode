package com.minileetcode.service;

import com.minileetcode.dto.StatsDto;
import com.minileetcode.dto.SubmissionDto;
import com.minileetcode.model.Submission;
import com.minileetcode.model.User;
import com.minileetcode.repository.SubmissionRepository;
import com.minileetcode.repository.UserRepository;
import com.minileetcode.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Service layer for Submission management.
 *
 * <p>Handles submission creation, retrieval, and statistics.
 * When a submission is created, the service:
 * <ol>
 *   <li>Simulates a judge result (for demo purposes)</li>
 *   <li>Saves the submission</li>
 *   <li>Updates problem acceptance rate</li>
 *   <li>Increments user solved count if ACCEPTED for the first time</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository       userRepository;
    private final ProblemRepository    problemRepository;
    private final ProblemService       problemService;
    private final UserService          userService;

    // Used to simulate random execution times
    private final Random random = new Random();

    // ----------------------------------------------------------------
    //  Create
    // ----------------------------------------------------------------

    /**
     * Submit a solution for a problem.
     *
     * <p>If no status is provided in the request, the service simulates
     * a judge verdict. Acceptance rates match real-world distributions:
     * ~50% ACCEPTED, ~30% WRONG_ANSWER, ~12% TIME_LIMIT, ~8% COMPILE_ERROR.
     *
     * @param dto the submission request body
     * @return saved submission DTO with assigned ID, status, and timing
     * @throws RuntimeException if user or problem do not exist
     */
    @Transactional
    public SubmissionDto createSubmission(SubmissionDto dto) {
        // Validate that the user and problem exist
        if (!userRepository.existsById(dto.getUserId())) {
            throw new RuntimeException("User not found with id: " + dto.getUserId());
        }
        if (!problemRepository.existsById(dto.getProblemId())) {
            throw new RuntimeException("Problem not found with id: " + dto.getProblemId());
        }

        Submission submission = new Submission();
        submission.setUserId(dto.getUserId());
        submission.setProblemId(dto.getProblemId());
        submission.setCode(dto.getCode());
        submission.setLanguage(dto.getLanguage());

        // Determine status: use provided status or simulate a verdict
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                submission.setStatus(Submission.Status.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Fall back to simulation if an invalid status string is provided
                submission.setStatus(simulateVerdict());
            }
        } else {
            // No status provided - simulate a judge verdict for demo realism
            submission.setStatus(simulateVerdict());
        }

        // Simulate execution time (only meaningful for ACCEPTED / TIME_LIMIT)
        submission.setExecutionTimeMs(simulateExecutionTime(submission.getStatus()));

        Submission saved = submissionRepository.save(submission);

        // --- Side effects after saving ---

        // Recalculate problem acceptance rate
        long total    = submissionRepository.countByProblemId(dto.getProblemId());
        long accepted = submissionRepository.countByProblemIdAndStatus(
                dto.getProblemId(), Submission.Status.ACCEPTED);
        problemService.updateAcceptanceRate(dto.getProblemId(), total, accepted);

        // If this is an ACCEPTED submission, check if it's the first solve for this user
        if (saved.getStatus() == Submission.Status.ACCEPTED) {
            boolean alreadySolvedBefore = submissionRepository
                    .existsAcceptedByUserIdAndProblemId(dto.getUserId(), dto.getProblemId());
            // Note: the saved submission IS counted, so "already solved" means count > 1
            long acceptedCount = submissionRepository.countByUserIdAndStatus(
                    dto.getUserId(), Submission.Status.ACCEPTED);
            if (acceptedCount == 1) {
                // First ever accept for this user on any problem
                userService.incrementSolvedCount(dto.getUserId());
            } else {
                // Check if this specific problem was solved before this submission
                // We do this by seeing if there's > 1 ACCEPTED for this user+problem
                long solvedThisProblemCount = submissionRepository
                        .findByUserIdOrderBySubmittedAtDesc(dto.getUserId())
                        .stream()
                        .filter(s -> s.getProblemId().equals(dto.getProblemId())
                                && s.getStatus() == Submission.Status.ACCEPTED)
                        .count();
                if (solvedThisProblemCount == 1) {
                    // This is the FIRST accepted submission for this problem by this user
                    userService.incrementSolvedCount(dto.getUserId());
                }
            }
        }

        return SubmissionDto.fromEntity(saved);
    }

    // ----------------------------------------------------------------
    //  Read
    // ----------------------------------------------------------------

    /**
     * Retrieve all submissions by a specific user, most-recent first.
     *
     * @param userId the user's ID
     * @return list of submission DTOs enriched with problem title
     */
    public List<SubmissionDto> getSubmissionsByUser(Long userId) {
        return submissionRepository
                .findByUserIdOrderBySubmittedAtDesc(userId)
                .stream()
                .map(s -> enrichWithNames(SubmissionDto.fromEntity(s)))
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all submissions for a specific problem, most-recent first.
     *
     * @param problemId the problem's ID
     * @return list of submission DTOs enriched with username
     */
    public List<SubmissionDto> getSubmissionsByProblem(Long problemId) {
        return submissionRepository
                .findByProblemIdOrderBySubmittedAtDesc(problemId)
                .stream()
                .map(s -> enrichWithNames(SubmissionDto.fromEntity(s)))
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a single submission by ID.
     *
     * @param id the submission's ID
     * @return submission DTO
     * @throws RuntimeException if not found
     */
    public SubmissionDto getSubmissionById(Long id) {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Submission not found with id: " + id));
        return enrichWithNames(SubmissionDto.fromEntity(submission));
    }

    // ----------------------------------------------------------------
    //  Statistics
    // ----------------------------------------------------------------

    /**
     * Compute submission statistics for a given user.
     *
     * <p>Returns total submissions, accepted count, unique problems solved,
     * acceptance rate, and a breakdown by status.
     *
     * @param userId the user's ID
     * @return populated StatsDto
     * @throws RuntimeException if user not found
     */
    public StatsDto getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        long total    = submissionRepository.countByUserId(userId);
        long accepted = submissionRepository.countByUserIdAndStatus(userId, Submission.Status.ACCEPTED);
        long wrong    = submissionRepository.countByUserIdAndStatus(userId, Submission.Status.WRONG_ANSWER);
        long tle      = submissionRepository.countByUserIdAndStatus(userId, Submission.Status.TIME_LIMIT);
        long ce       = submissionRepository.countByUserIdAndStatus(userId, Submission.Status.COMPILE_ERROR);
        long unique   = submissionRepository.countDistinctProblemsSolvedByUser(userId);

        // Acceptance rate as a percentage, rounded to 1 decimal place
        double rate = total > 0 ? Math.round((accepted * 100.0 / total) * 10.0) / 10.0 : 0.0;

        return new StatsDto(
                userId,
                user.getUsername(),
                total,
                accepted,
                unique,
                rate,
                wrong,
                tle,
                ce
        );
    }

    // ----------------------------------------------------------------
    //  Private helpers
    // ----------------------------------------------------------------

    /**
     * Simulate a judge verdict using a weighted random selection.
     * Weights approximate real competitive programming acceptance distributions.
     *
     * @return simulated Submission.Status
     */
    private Submission.Status simulateVerdict() {
        int roll = random.nextInt(100); // 0-99
        if (roll < 50)       return Submission.Status.ACCEPTED;       // 50%
        else if (roll < 80)  return Submission.Status.WRONG_ANSWER;   // 30%
        else if (roll < 92)  return Submission.Status.TIME_LIMIT;     // 12%
        else                 return Submission.Status.COMPILE_ERROR;  //  8%
    }

    /**
     * Simulate an execution time based on the verdict.
     * ACCEPTED runs are fast; TIME_LIMIT runs hit the ceiling.
     *
     * @param status the verdict
     * @return execution time in milliseconds
     */
    private int simulateExecutionTime(Submission.Status status) {
        return switch (status) {
            case ACCEPTED       -> 50  + random.nextInt(200);  // 50-249 ms
            case WRONG_ANSWER   -> 80  + random.nextInt(300);  // 80-379 ms
            case TIME_LIMIT     -> 2000 + random.nextInt(500); // 2000-2499 ms (TLE)
            case COMPILE_ERROR  -> 0;                          // didn't run
        };
    }

    /**
     * Enrich a SubmissionDto with the username and problem title by
     * looking up the related entities. Falls back to IDs if not found.
     *
     * @param dto the DTO to enrich
     * @return the same DTO with username and problemTitle populated
     */
    private SubmissionDto enrichWithNames(SubmissionDto dto) {
        // Look up username
        userRepository.findById(dto.getUserId())
                .ifPresent(u -> dto.setUsername(u.getUsername()));

        // Look up problem title
        problemRepository.findById(dto.getProblemId())
                .ifPresent(p -> dto.setProblemTitle(p.getTitle()));

        return dto;
    }
}
