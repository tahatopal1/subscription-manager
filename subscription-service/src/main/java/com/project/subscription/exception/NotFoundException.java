package com.project.subscription.exception;

/**
 * Thrown when a requested resource cannot be found.
 * Caught and mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
