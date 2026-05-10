package com.project.subscription.exception;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error response envelope returned to all API clients.
 * Per 03-coding-security.md §1: error responses MUST follow a standardized JSON format.
 */
public record ApiError(
        int status,
        String error,
        List<String> messages,
        String path,
        Instant timestamp
) {

    /**
     * Convenience factory for a single-message error.
     */
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(status, error, List.of(message), path, Instant.now());
    }

    /**
     * Convenience factory for multiple validation messages.
     */
    public static ApiError of(int status, String error, List<String> messages, String path) {
        return new ApiError(status, error, messages, path, Instant.now());
    }
}
