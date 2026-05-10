package com.project.subscription.exception;

/**
 * Thrown when a request conflicts with the current state of a resource.
 * Mapped to HTTP 409 Conflict by GlobalExceptionHandler.
 * Example: a user attempting to create a new subscription while one is still active.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
