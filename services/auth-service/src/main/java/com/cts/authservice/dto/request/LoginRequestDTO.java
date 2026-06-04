package com.cts.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request payload carrying credentials for a login attempt.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    @NotBlank(message = "Username or Email is required for login")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required for login")
    private String password;

}
