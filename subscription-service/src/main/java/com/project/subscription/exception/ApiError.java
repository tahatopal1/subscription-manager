package com.project.subscription.exception;

import java.time.Instant;
import java.util.List;

public record ApiError(
        int status,
        String error,
        List<String> messages,
        String path,
        Instant timestamp
) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(status, error, List.of(message), path, Instant.now());
    }

    public static ApiError of(int status, String error, List<String> messages, String path) {
        return new ApiError(status, error, messages, path, Instant.now());
    }
}
