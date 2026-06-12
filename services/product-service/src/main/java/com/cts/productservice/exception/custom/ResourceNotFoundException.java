package com.cts.productservice.exception.custom;

/**
 * Thrown when a requested resource (product or category) cannot be located by the
 * given identifier or name.
 * <p>
 * Mapped to HTTP {@code 404 NOT FOUND} by
 * {@link com.cts.productservice.exception.GlobalExceptionHandler}.
 *
 * @since 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates the exception with a detail message identifying the missing resource.
     *
     * @param message the detail message describing what could not be found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
