package com.cts.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request payload holding the optionally-updatable fields of a user.
 */
@Data
public class UpdateUserDTO {

    @Size(min = 2, max = 100)
    private String name;

    @Email
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "phoneNumber must be 10 digits")
    private String phoneNumber;

    @Past
    private LocalDate dateOfBirth;
}