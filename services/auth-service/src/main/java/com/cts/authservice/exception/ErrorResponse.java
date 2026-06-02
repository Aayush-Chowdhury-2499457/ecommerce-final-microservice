package com.cts.authservice.exception;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Standard error payload returned to clients describing a failed request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String errorClass;
    private String message;
    private String path;
}
