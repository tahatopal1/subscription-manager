package com.project.auth.service;

import com.project.auth.dto.request.auth.LoginRequest;
import com.project.auth.dto.request.auth.RegisterRequest;
import com.project.auth.dto.response.LoginResponse;
import com.project.auth.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}
