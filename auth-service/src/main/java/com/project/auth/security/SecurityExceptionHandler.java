package com.project.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.auth.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        log.warn("Unauthorized request to {}: {}", request.getRequestURI(), authException.getMessage());
        writeError(response, HttpStatus.UNAUTHORIZED, "Unauthorized",
                "Authentication is required to access this resource.", request.getRequestURI());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Access denied to {}: {}", request.getRequestURI(), accessDeniedException.getMessage());
        writeError(response, HttpStatus.FORBIDDEN, "Forbidden",
                "You do not have permission to access this resource.", request.getRequestURI());
    }

    private void writeError(HttpServletResponse response, HttpStatus status,
            String error, String message, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError apiError = ApiError.of(status.value(), error, message, path);
        objectMapper.writeValue(response.getOutputStream(), apiError);
    }
}
