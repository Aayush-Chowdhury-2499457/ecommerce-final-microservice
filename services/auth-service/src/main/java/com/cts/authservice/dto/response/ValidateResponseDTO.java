package com.cts.authservice.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateResponseDTO {
    private Long userId;
    private String role;
}