package com.cts.authservice.dto.response;

import lombok.*;
import java.time.LocalDate;

/**
 * Response payload returning the created user's profile details after registration.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponseDTO {
    private Long userId;
    private String name;
    private String username;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
}
