package com.project.auth.exception;

/**
 * Thrown when identity/business rule violations occur (e.g., email already registered,
 * account is locked). Mapped to HTTP 422 by GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
