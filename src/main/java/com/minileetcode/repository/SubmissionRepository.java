package com.minileetcode.repository;

import com.minileetcode.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Submission entity.
 *
 * <p>Provides user-centric and problem-centric submission queries,
 * plus aggregate queries for statistics.
 */
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    /**
     * Get all submissions by a specific user, ordered most-recent first.
     *
     * @param userId the user's ID
     * @return list of submissions
     */
    List<Submission> findByUserIdOrderBySubmittedAtDesc(Long userId);

    /**
     * Get all submissions for a specific problem, ordered most-recent first.
     *
     * @param problemId the problem's ID
     * @return list of submissions
     */
    List<Submission> findByProblemIdOrderBySubmittedAtDesc(Long problemId);

    /**
     * Count how many submissions a user has for a specific status.
     * Used in statistics computation.
     */
    long countByUserIdAndStatus(Long userId, Submission.Status status);

    /**
     * Count the total number of submissions made by a user.
     */
    long countByUserId(Long userId);

    /**
     * Find the distinct problem IDs a user has solved (at least one ACCEPTED).
     * The DISTINCT ensures each problem is counted only once.
     */
    @Query("SELECT COUNT(DISTINCT s.problemId) FROM Submission s " +
           "WHERE s.userId = :userId AND s.status = 'ACCEPTED'")
    long countDistinctProblemsSolvedByUser(@Param("userId") Long userId);

    /**
     * Check whether a user has already solved a problem (has an ACCEPTED submission).
     */
    @Query("SELECT COUNT(s) > 0 FROM Submission s " +
           "WHERE s.userId = :userId AND s.problemId = :problemId AND s.status = 'ACCEPTED'")
    boolean existsAcceptedByUserIdAndProblemId(
            @Param("userId") Long userId,
            @Param("problemId") Long problemId);

    /**
     * Count total accepted submissions for a problem (for acceptance rate calculation).
     */
    long countByProblemIdAndStatus(Long problemId, Submission.Status status);

    /**
     * Count total submissions for a problem.
     */
    long countByProblemId(Long problemId);
}
