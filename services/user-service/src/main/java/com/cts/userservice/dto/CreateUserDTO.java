package com.cts.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request payload carrying the fields required to register a new user.
 */
@Data
public class CreateUserDTO {

    @Size(min = 2, max = 100)
    private String name;

    @NotBlank
    @Size(min = 3, max = 100)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$", message = "phoneNumber must be 10 digits")
    private String phoneNumber;

    @Past
    private LocalDate dateOfBirth;
}