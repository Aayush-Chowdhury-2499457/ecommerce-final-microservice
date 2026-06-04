package com.cts.authservice.dto.request;

import lombok.*;
import java.time.LocalDate;

/**
 * Request payload sent to the User Service to create a new user profile.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequestDTO {
    private String name;
    private String username;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
}