package com.project.auth.service;

import com.project.auth.dto.request.auth.LoginRequest;
import com.project.auth.dto.request.auth.RegisterRequest;
import com.project.auth.dto.response.LoginResponse;
import com.project.auth.dto.response.UserResponse;

/**
 * Contract for public authentication operations (register and login).
 * Per .agentrules: all business logic lives in the Service layer, never the Controller.
 */
public interface AuthService {

    /**
     * Registers a new user account with the ROLE_USER role.
     * Throws BusinessException if the email is already registered.
     *
     * @param request validated registration payload
     * @return the persisted user profile (no password hash exposed)
     */
    UserResponse register(RegisterRequest request);

    /**
     * Authenticates a user by email/password and issues a signed JWT.
     * Throws BusinessException if the account is locked.
     *
     * @param request validated login payload
     * @return signed JWT wrapped in a LoginResponse
     */
    LoginResponse login(LoginRequest request);
}
