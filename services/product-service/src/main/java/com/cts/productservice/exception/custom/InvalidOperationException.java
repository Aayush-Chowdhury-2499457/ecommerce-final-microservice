package com.cts.productservice.exception.custom;

/**
 * Thrown when a requested operation is not permitted in the current state (for
 * example, deleting a category that still has products attached, or reducing stock
 * below zero).
 * <p>
 * Mapped to HTTP {@code 400 BAD REQUEST} by
 * {@link com.cts.productservice.exception.GlobalExceptionHandler}.
 *
 * @since 1.0
 */
public class InvalidOperationException extends RuntimeException {

    /**
     * Creates the exception with a detail message describing why the operation is invalid.
     *
     * @param message the detail message explaining the invalid operation
     */
    public InvalidOperationException(String message) {
        super(message);
    }
}
