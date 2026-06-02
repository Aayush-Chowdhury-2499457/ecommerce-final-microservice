package com.cts.authservice.dto.response;

import lombok.*;

/**
 * Response payload containing the issued JWT token after a successful login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String token;
}