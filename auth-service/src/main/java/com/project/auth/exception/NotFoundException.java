package com.project.auth.exception;

/**
 * Thrown when a requested identity resource (User) cannot be found.
 * Mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
