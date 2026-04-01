package com.minileetcode.controller;

import com.minileetcode.dto.StatsDto;
import com.minileetcode.dto.SubmissionDto;
import com.minileetcode.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for Submission APIs.
 *
 * <p>Exposes 5 endpoints:
 * <ul>
 *   <li>POST /api/submissions                          - submit a solution</li>
 *   <li>GET  /api/submissions/{id}                     - get a submission by ID</li>
 *   <li>GET  /api/submissions/user/{userId}            - get submissions by user</li>
 *   <li>GET  /api/submissions/problem/{problemId}      - get submissions for a problem</li>
 *   <li>GET  /api/submissions/user/{userId}/stats      - get user statistics</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "APIs for submitting solutions and viewing results")
@CrossOrigin(origins = "*")
public class SubmissionController {

    private final SubmissionService submissionService;

    // ----------------------------------------------------------------
    //  POST /api/submissions
    // ----------------------------------------------------------------

    @Operation(summary = "Submit a solution for a problem")
    @PostMapping
    public ResponseEntity<?> createSubmission(@Valid @RequestBody SubmissionDto dto) {
        try {
            SubmissionDto saved = submissionService.createSubmission(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    //  GET /api/submissions/{id}
    // ----------------------------------------------------------------

    @Operation(summary = "Get a submission by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(submissionService.getSubmissionById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    //  GET /api/submissions/user/{userId}
    // ----------------------------------------------------------------

    @Operation(summary = "Get all submissions by a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubmissionDto>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(submissionService.getSubmissionsByUser(userId));
    }

    // ----------------------------------------------------------------
    //  GET /api/submissions/problem/{problemId}
    // ----------------------------------------------------------------

    @Operation(summary = "Get all submissions for a specific problem")
    @GetMapping("/problem/{problemId}")
    public ResponseEntity<List<SubmissionDto>> getByProblem(@PathVariable Long problemId) {
        return ResponseEntity.ok(submissionService.getSubmissionsByProblem(problemId));
    }

    // ----------------------------------------------------------------
    //  GET /api/submissions/user/{userId}/stats
    // ----------------------------------------------------------------

    @Operation(summary = "Get submission statistics for a user")
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<?> getUserStats(@PathVariable Long userId) {
        try {
            StatsDto stats = submissionService.getUserStats(userId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
