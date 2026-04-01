package com.minileetcode.controller;

import com.minileetcode.dto.LoginRequest;
import com.minileetcode.dto.UserDto;
import com.minileetcode.service.UserService;
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
 * REST controller for User Management APIs.
 *
 * <p>Exposes 6 endpoints:
 * <ul>
 *   <li>POST   /api/users/register - register a new user</li>
 *   <li>POST   /api/users/login    - authenticate a user</li>
 *   <li>GET    /api/users          - list all users</li>
 *   <li>GET    /api/users/{id}     - get a user by ID</li>
 *   <li>PUT    /api/users/{id}     - update a user</li>
 *   <li>DELETE /api/users/{id}     - delete a user</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for registering, authenticating, and managing users")
@CrossOrigin(origins = "*") // Allow requests from any origin (for demo frontend)
public class UserController {

    private final UserService userService;

    // ----------------------------------------------------------------
    //  POST /api/users/register
    // ----------------------------------------------------------------

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDto dto) {
        try {
            UserDto created = userService.register(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            // Return 400 with a descriptive error message
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    //  POST /api/users/login
    // ----------------------------------------------------------------

    @Operation(summary = "Authenticate a user (returns user profile on success)")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            UserDto user = userService.login(request);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            // Return 401 Unauthorized for bad credentials
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    //  GET /api/users
    // ----------------------------------------------------------------

    @Operation(summary = "Get all users")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ----------------------------------------------------------------
    //  GET /api/users/{id}
    // ----------------------------------------------------------------

    @Operation(summary = "Get a user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    //  PUT /api/users/{id}
    // ----------------------------------------------------------------

    @Operation(summary = "Update a user's profile")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestBody UserDto dto) {
        try {
            UserDto updated = userService.updateUser(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    //  DELETE /api/users/{id}
    // ----------------------------------------------------------------

    @Operation(summary = "Delete a user")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
