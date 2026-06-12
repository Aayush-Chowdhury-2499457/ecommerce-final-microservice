package com.cts.productservice.exception.custom;

/**
 * Thrown when an attempt is made to create or rename a resource in a way that would
 * violate a uniqueness constraint (for example, a category name that already exists).
 * <p>
 * Mapped to HTTP {@code 409 CONFLICT} by
 * {@link com.cts.productservice.exception.GlobalExceptionHandler}.
 *
 * @since 1.0
 */
public class DuplicateResourceException extends RuntimeException {

    /**
     * Creates the exception with a detail message describing the conflict.
     *
     * @param message the detail message explaining which resource is duplicated
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}
