package com.project.auth.dto.response;

import com.project.auth.entity.User;

import java.time.Instant;
import java.util.Set;


public record UserResponse(
        Long id,
        String email,
        String name,
        String surname,
        Set<String> roles,
        boolean isLocked,
        Instant createdAt,
        Instant updatedAt
) {

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
