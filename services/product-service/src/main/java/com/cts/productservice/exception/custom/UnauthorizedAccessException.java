package com.cts.productservice.exception.custom;

/**
 * Thrown when the caller lacks the role or ownership required to perform an
 * operation (for example, a non-admin attempting an admin-only endpoint).
 * <p>
 * Mapped to HTTP {@code 403 FORBIDDEN} by
 * {@link com.cts.productservice.exception.GlobalExceptionHandler}.
 *
 * @since 1.0
 */
public class UnauthorizedAccessException extends RuntimeException {

    /**
     * Creates the exception with a detail message describing the denied access.
     *
     * @param message the detail message explaining why access was denied
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
