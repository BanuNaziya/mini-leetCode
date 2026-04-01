package com.minileetcode.dto;

import com.minileetcode.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for User.
 *
 * <p>Used for both incoming requests (register/update) and outgoing responses.
 * The passwordHash field is intentionally excluded from responses - it is only
 * used when processing register/update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /** Present in responses; null when used as a create request body */
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 120, message = "Email must be at most 120 characters")
    private String email;

    /**
     * Plain-text password supplied during registration or update.
     * The service layer will hash this before persisting.
     * Never returned in responses.
     */
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    /** Role string: "USER" or "ADMIN" */
    private String role;

    /** Set by the server on responses */
    private LocalDateTime createdAt;
    private Integer totalSolved;
    private String rank;

    /**
     * Converts a User entity to a UserDto for API responses.
     * Password hash is intentionally excluded.
     *
     * @param user the User entity
     * @return populated UserDto (no password)
     */
    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name() : "USER");
        dto.setCreatedAt(user.getCreatedAt());
        dto.setTotalSolved(user.getTotalSolved());
        dto.setRank(user.getRank());
        // password is deliberately not mapped
        return dto;
    }
}
