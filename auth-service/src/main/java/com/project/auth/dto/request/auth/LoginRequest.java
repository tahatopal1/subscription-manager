package com.project.auth.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload for user login.
 * Validated at the Controller level using @Valid (03-coding-security.md §3).
 */
public record LoginRequest(

        @NotBlank(message = "email must not be blank")
        @Email(message = "email must be a valid email address")
        String email,

        @NotBlank(message = "password must not be blank")
        String password
) {
}
