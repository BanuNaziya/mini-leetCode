package com.minileetcode.repository;

import com.minileetcode.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Problem entity.
 *
 * <p>Provides querying by difficulty and tag search using JPQL.
 */
@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    /**
     * Retrieve all problems with a specific difficulty level.
     *
     * @param difficulty the difficulty enum value
     * @return list of matching problems
     */
    List<Problem> findByDifficulty(Problem.Difficulty difficulty);

    /**
     * Search problems by a tag substring.
     * The tags column stores comma-separated values, so we use LIKE for a
     * simple contains search. For production, normalise tags into a separate table.
     *
     * @param tag the tag keyword to search for
     * @return list of problems whose tags column contains the keyword
     */
    @Query("SELECT p FROM Problem p WHERE LOWER(p.tags) LIKE LOWER(CONCAT('%', :tag, '%'))")
    List<Problem> findByTagContaining(@Param("tag") String tag);

    /**
     * Count problems grouped by difficulty - used for dashboard stats.
     */
    long countByDifficulty(Problem.Difficulty difficulty);
}
