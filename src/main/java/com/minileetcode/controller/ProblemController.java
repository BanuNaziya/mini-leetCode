package com.minileetcode.controller;

import com.minileetcode.dto.ProblemDto;
import com.minileetcode.service.ProblemService;
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
 * REST controller for Problem Management APIs.
 *
 * <p>Exposes 7 endpoints:
 * <ul>
 *   <li>GET    /api/problems                         - list all problems</li>
 *   <li>GET    /api/problems/{id}                    - get a problem by ID</li>
 *   <li>POST   /api/problems                         - create a problem</li>
 *   <li>PUT    /api/problems/{id}                    - update a problem</li>
 *   <li>DELETE /api/problems/{id}                    - delete a problem</li>
 *   <li>GET    /api/problems/difficulty/{level}      - filter by difficulty</li>
 *   <li>GET    /api/problems/search?tag={tag}        - search by tag</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
@Tag(name = "Problem Management", description = "APIs for creating, reading, updating, and filtering problems")
@CrossOrigin(origins = "*")
public class ProblemController {

    private final ProblemService problemService;

    // ----------------------------------------------------------------
    //  GET /api/problems
    // ----------------------------------------------------------------

    @Operation(summary = "Get all problems")
    @GetMapping
    public ResponseEntity<List<ProblemDto>> getAllProblems() {
        return ResponseEntity.ok(problemService.getAllProblems());
    }

    // ----------------------------------------------------------------
    //  GET /api/problems/{id}
    // ----------------------------------------------------------------

    @Operation(summary = "Get a problem by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getProblemById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(problemService.getProblemById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    //  POST /api/problems
    // ----------------------------------------------------------------

    @Operation(summary = "Create a new problem (Admin)")
    @PostMapping
    public ResponseEntity<?> createProblem(@Valid @RequestBody ProblemDto dto) {
        try {
            ProblemDto created = problemService.createProblem(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    //  PUT /api/problems/{id}
    // ----------------------------------------------------------------

    @Operation(summary = "Update an existing problem (Admin)")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProblem(@PathVariable Long id,
                                           @RequestBody ProblemDto dto) {
        try {
            ProblemDto updated = problemService.updateProblem(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    //  DELETE /api/problems/{id}
    // ----------------------------------------------------------------

    @Operation(summary = "Delete a problem (Admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProblem(@PathVariable Long id) {
        try {
            problemService.deleteProblem(id);
            return ResponseEntity.ok(Map.of("message", "Problem deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    //  GET /api/problems/difficulty/{level}
    // ----------------------------------------------------------------

    @Operation(summary = "Filter problems by difficulty level (EASY, MEDIUM, HARD)")
    @GetMapping("/difficulty/{level}")
    public ResponseEntity<?> getByDifficulty(@PathVariable String level) {
        try {
            List<ProblemDto> problems = problemService.getProblemsByDifficulty(level);
            return ResponseEntity.ok(problems);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    //  GET /api/problems/search?tag={tag}
    // ----------------------------------------------------------------

    @Operation(summary = "Search problems by tag keyword")
    @GetMapping("/search")
    public ResponseEntity<List<ProblemDto>> searchByTag(
            @RequestParam(name = "tag", required = false, defaultValue = "") String tag) {
        return ResponseEntity.ok(problemService.searchByTag(tag));
    }
}
