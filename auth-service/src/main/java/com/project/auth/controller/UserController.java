package com.project.auth.controller;

import com.project.auth.dto.request.user.UpdateProfileRequest;
import com.project.auth.dto.request.user.UserPasswordUpdateRequest;
import com.project.auth.dto.response.UserResponse;
import com.project.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getProfile(Principal principal) {
        String userId = principal.getName();
        log.info("User requesting their own profile for ID: {}", userId);
        
        UserResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request, Principal principal) {
        String userId = principal.getName();
        log.info("User updating their own profile for ID: {}", userId);
        
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> renewPassword(@Valid @RequestBody UserPasswordUpdateRequest request, Principal principal) {
        String userId = principal.getName();
        log.info("User renewing password for ID: {}", userId);
        
        userService.renewPassword(userId, request);
        return ResponseEntity.noContent().build();
    }
}
