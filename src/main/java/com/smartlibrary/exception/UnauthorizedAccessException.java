package com.smartlibrary.exception;

/**
 * Thrown when a logged-in user attempts an action outside their role
 * (e.g. a student requesting a librarian-only endpoint).
 */
public class UnauthorizedAccessException extends Exception {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
