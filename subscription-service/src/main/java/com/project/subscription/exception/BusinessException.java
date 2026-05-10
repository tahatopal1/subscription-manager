package com.project.subscription.exception;

/**
 * Thrown when domain/business rule violations occur (e.g., invalid state transition).
 * Caught and mapped to a standardized HTTP 422 response by GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
