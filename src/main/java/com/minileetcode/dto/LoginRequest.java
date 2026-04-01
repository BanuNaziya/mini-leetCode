package com.minileetcode.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body DTO for POST /api/users/login.
 *
 * <p>Accepts either username or email together with a plain-text password.
 * In a production app this endpoint would return a JWT token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * The user's username OR email address.
     * The service will attempt to find the user by either field.
     */
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    private String password;
}
