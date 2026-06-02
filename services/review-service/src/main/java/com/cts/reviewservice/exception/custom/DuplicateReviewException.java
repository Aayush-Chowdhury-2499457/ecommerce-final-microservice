package com.cts.reviewservice.exception.custom;

/**
 * Thrown when a user attempts to review a product they have already reviewed.
 */
public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException(String message) {
        super(message);
    }
}