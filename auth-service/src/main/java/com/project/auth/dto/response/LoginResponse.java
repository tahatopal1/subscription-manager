package com.project.auth.dto.response;

/**
 * Response returned to the client after a successful login.
 * Contains the signed JWT token for use in subsequent requests.
 */
public record LoginResponse(
        String accessToken,
        String tokenType
) {

    public static LoginResponse of(String accessToken) {
        return new LoginResponse(accessToken, "Bearer");
    }
}
