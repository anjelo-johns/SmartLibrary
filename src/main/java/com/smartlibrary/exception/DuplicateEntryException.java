package com.smartlibrary.exception;

/**
 * Thrown when a registration or insert would violate a uniqueness
 * rule (duplicate register number, email, or ISBN).
 */
public class DuplicateEntryException extends Exception {
    public DuplicateEntryException(String message) {
        super(message);
    }
}
