package com.minileetcode.service;

import com.minileetcode.dto.LoginRequest;
import com.minileetcode.dto.UserDto;
import com.minileetcode.model.User;
import com.minileetcode.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for User management.
 *
 * <p>Contains all business logic for user operations. The controller delegates
 * to this class, keeping the controller thin. Password hashing is done here
 * using a simple approach (SHA-256 hex) to avoid Spring Security dependencies.
 * In production, use BCryptPasswordEncoder.
 */
@Service
@RequiredArgsConstructor // Lombok: injects dependencies via constructor
public class UserService {

    private final UserRepository userRepository;

    // ----------------------------------------------------------------
    //  Registration
    // ----------------------------------------------------------------

    /**
     * Register a new user.
     *
     * <p>Validates that username and email are not already taken,
     * hashes the password, then saves the user.
     *
     * @param dto incoming registration request
     * @return the created user as a DTO (no password)
     * @throws IllegalArgumentException if username or email is already in use
     */
    @Transactional
    public UserDto register(UserDto dto) {
        // Check for duplicate username
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username '" + dto.getUsername() + "' is already taken.");
        }
        // Check for duplicate email
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already registered.");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        // Hash the plain-text password before storage
        user.setPasswordHash(hashPassword(dto.getPassword()));

        // Assign role - default to USER unless explicitly set to ADMIN
        if ("ADMIN".equalsIgnoreCase(dto.getRole())) {
            user.setRole(User.Role.ADMIN);
        } else {
            user.setRole(User.Role.USER);
        }

        user.setTotalSolved(0);
        user.setRank(computeRank(0));

        User saved = userRepository.save(user);
        return UserDto.fromEntity(saved);
    }

    // ----------------------------------------------------------------
    //  Login
    // ----------------------------------------------------------------

    /**
     * Authenticate a user by username/email and password.
     *
     * <p>NOTE: In a production app this would return a signed JWT token.
     * For this portfolio project it returns the user DTO on success.
     *
     * @param request login credentials
     * @return authenticated user DTO
     * @throws IllegalArgumentException if credentials are invalid
     */
    public UserDto login(LoginRequest request) {
        // Try to find user by username or email
        User user = userRepository
                .findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username/email or password."));

        // Compare hashed password
        if (!hashPassword(request.getPassword()).equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username/email or password.");
        }

        return UserDto.fromEntity(user);
    }

    // ----------------------------------------------------------------
    //  Read
    // ----------------------------------------------------------------

    /**
     * Retrieve all registered users.
     *
     * @return list of all users as DTOs
     */
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve a user by ID.
     *
     * @param id the user's ID
     * @return user DTO
     * @throws RuntimeException if no user found with the given ID
     */
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return UserDto.fromEntity(user);
    }

    // ----------------------------------------------------------------
    //  Update
    // ----------------------------------------------------------------

    /**
     * Update an existing user's profile.
     *
     * <p>Only the fields present in the DTO are updated. Password is only
     * changed if a new non-blank password is supplied.
     *
     * @param id  the ID of the user to update
     * @param dto updated fields
     * @return updated user DTO
     * @throws RuntimeException if no user found with the given ID
     */
    @Transactional
    public UserDto updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Update username if changed and not taken by another user
        if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new IllegalArgumentException("Username '" + dto.getUsername() + "' is already taken.");
            }
            user.setUsername(dto.getUsername());
        }

        // Update email if changed and not taken by another user
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already registered.");
            }
            user.setEmail(dto.getEmail());
        }

        // Update password only if a new one is supplied
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPasswordHash(hashPassword(dto.getPassword()));
        }

        // Update role if provided
        if (dto.getRole() != null) {
            user.setRole(User.Role.valueOf(dto.getRole().toUpperCase()));
        }

        User updated = userRepository.save(user);
        return UserDto.fromEntity(updated);
    }

    // ----------------------------------------------------------------
    //  Delete
    // ----------------------------------------------------------------

    /**
     * Delete a user by ID.
     *
     * @param id the user's ID
     * @throws RuntimeException if no user found with the given ID
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // ----------------------------------------------------------------
    //  Internal helpers
    // ----------------------------------------------------------------

    /**
     * Increment a user's totalSolved count and update their rank.
     * Called by SubmissionService when an ACCEPTED submission is recorded.
     *
     * @param userId the user whose count to increment
     */
    @Transactional
    public void incrementSolvedCount(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            int newCount = user.getTotalSolved() + 1;
            user.setTotalSolved(newCount);
            user.setRank(computeRank(newCount));
            userRepository.save(user);
        });
    }

    /**
     * Hash a plain-text password using SHA-256.
     * NOTE: Use BCrypt in production for proper salting.
     *
     * @param plainText the plain-text password
     * @return hex-encoded SHA-256 hash
     */
    private String hashPassword(String plainText) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // SHA-256 is always available in the JVM
            throw new RuntimeException("Could not hash password", e);
        }
    }

    /**
     * Compute a rank label based on the number of problems solved.
     *
     * @param totalSolved number of accepted problems
     * @return rank label string
     */
    private String computeRank(int totalSolved) {
        if (totalSolved >= 200) return "Grandmaster";
        if (totalSolved >= 100) return "Expert";
        if (totalSolved >= 50)  return "Advanced";
        if (totalSolved >= 20)  return "Intermediate";
        if (totalSolved >= 5)   return "Apprentice";
        return "Beginner";
    }
}
