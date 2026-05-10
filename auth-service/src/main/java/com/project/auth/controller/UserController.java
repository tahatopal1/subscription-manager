package com.project.auth.controller;

import com.project.auth.dto.request.user.UpdateProfileRequest;
import com.project.auth.dto.request.user.UserPasswordUpdateRequest;
import com.project.auth.dto.response.UserResponse;
import com.project.auth.security.CustomUserDetails;
import com.project.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("User requesting their own profile for ID: {}", currentUser.getId());
        return ResponseEntity.ok(userService.getProfile(currentUser.getId()));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("User updating their own profile for ID: {}", currentUser.getId());
        return ResponseEntity.ok(userService.updateProfile(currentUser.getId(), request));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> renewPassword(
            @Valid @RequestBody UserPasswordUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("User renewing password for ID: {}", currentUser.getId());
        userService.renewPassword(currentUser.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
