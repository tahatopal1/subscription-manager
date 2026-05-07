package com.project.auth.dto.response;

import com.project.auth.entity.User;

import java.time.Instant;
import java.util.Set;

/**
 * Response payload representing a user's identity profile.
 * NEVER includes password — this is a read-only projection safe for API responses.
 */
public record UserResponse(
        String id,
        String email,
        String name,
        String surname,
        Set<String> roles,
        boolean isLocked,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * Static factory to map a User entity to a UserResponse.
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getSurname(),
                user.getRoles(),
                user.isLocked(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
