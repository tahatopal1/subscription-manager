package com.project.auth.dto.request.admin;

/**
 * Payload for searching users with optional filters.
 * Null fields indicate no filter for that specific attribute.
 */
public record UserSearchRequest(
        String email,
        String name,
        String surname,
        String role
) {
}
