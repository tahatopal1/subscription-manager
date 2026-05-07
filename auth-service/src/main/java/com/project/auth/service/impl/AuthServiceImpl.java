package com.project.auth.service.impl;

import com.project.auth.dto.request.auth.LoginRequest;
import com.project.auth.dto.request.auth.RegisterRequest;
import com.project.auth.dto.response.LoginResponse;
import com.project.auth.dto.response.UserResponse;
import com.project.auth.entity.User;
import com.project.auth.exception.BusinessException;
import com.project.auth.repository.UserRepository;
import com.project.auth.security.JwtService;
import com.project.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final String ROLE_USER = "ROLE_USER";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Attempting to register new user with email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: Email {} is already registered", request.email());
            throw new BusinessException("Email is already in use.");
        }

        User user = User.builder()
                .email(request.email())
                .name(request.name())
                .surname(request.surname())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(ROLE_USER))
                .isLocked(false)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getId());

        return UserResponse.from(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Attempting login for email: {}", request.email());

        // This will authenticate the user using UserDetailsServiceImpl and throw exceptions if failed or locked.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Invalid credentials"));

        String token = jwtService.generateToken(user);
        
        log.info("User logged in successfully with ID: {}", user.getId());

        return new LoginResponse(token, "Bearer");
    }
}
