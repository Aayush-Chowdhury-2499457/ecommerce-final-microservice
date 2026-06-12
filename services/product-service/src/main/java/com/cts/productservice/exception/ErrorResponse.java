package com.cts.productservice.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized error payload returned to clients when a request fails.
 * <p>
 * Instances are assembled by {@link GlobalExceptionHandler} and serialized as the
 * JSON body of every non-successful HTTP response, giving callers a consistent,
 * machine-readable error structure. Lombok generates the accessors, constructors,
 * and builder for this class.
 *
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    /** Moment at which the error response was generated. */
    private LocalDateTime timestamp;

    /** Numeric HTTP status code associated with the failure (for example {@code 404}). */
    private Integer status;

    /** Human-readable HTTP status reason phrase (for example {@code "Not Found"}). */
    private String error;

    /** Simple name of the exception class that triggered this response. */
    private String errorClass;

    /** Detailed, client-facing description of what went wrong. */
    private String message;

    /** Request URI that produced the error. */
    private String path;
}
