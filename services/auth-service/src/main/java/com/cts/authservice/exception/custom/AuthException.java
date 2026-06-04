package com.cts.authservice.exception.custom;

/**
 * Thrown when authentication fails (e.g. invalid credentials or malformed token).
 */
public class AuthException extends RuntimeException {
    /**
     * Creates an authentication exception with the given message.
     *
     * @param message the error detail
     */
    public AuthException(String message) {
        super(message);
    }
}