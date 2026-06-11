package com.cts.productservice.exception.custom;

/**
 * Thrown when creating a resource that already exists (mapped to HTTP 409).
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
