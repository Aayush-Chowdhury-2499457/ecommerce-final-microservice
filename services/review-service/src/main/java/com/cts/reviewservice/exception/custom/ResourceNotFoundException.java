package com.cts.reviewservice.exception.custom;

/**
 * Thrown when a requested resource (e.g. a review) cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}