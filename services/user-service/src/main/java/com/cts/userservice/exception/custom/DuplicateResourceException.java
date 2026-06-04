package com.cts.userservice.exception.custom;

/**
 * Thrown when an attempt is made to create a resource that already exists.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}