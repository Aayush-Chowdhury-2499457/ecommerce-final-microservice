package com.cts.authservice.dto.response;

import lombok.*;

/**
 * Response payload returning the user id and role resolved from a validated token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateResponseDTO {
    private Long userId;
    private String role;
}