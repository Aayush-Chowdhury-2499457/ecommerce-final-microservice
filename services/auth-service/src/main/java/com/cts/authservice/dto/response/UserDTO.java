package com.cts.authservice.dto.response;

import lombok.*;

/**
 * Response payload representing user details fetched from the User Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long userId;
    private String username;
    private String email;
    private String role;
}