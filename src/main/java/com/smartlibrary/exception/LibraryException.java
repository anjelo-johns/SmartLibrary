package com.smartlibrary.exception;

/**
 * Thrown for any business-rule violation (book unavailable, duplicate
 * issue, borrowing limit reached, invalid copy count, etc). Servlets
 * catch this and show the message as a friendly, user-facing error —
 * never a raw stack trace.
 */
public class LibraryException extends Exception {
    public LibraryException(String message) {
        super(message);
    }
}
