package com.project.auth.controller;

import com.project.auth.dto.request.admin.AdminPasswordUpdateRequest;
import com.project.auth.dto.request.admin.AdminUserCreateRequest;
import com.project.auth.dto.request.admin.AdminUserUpdateRequest;
import com.project.auth.dto.request.admin.UserSearchRequest;
import com.project.auth.dto.response.UserResponse;
import com.project.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserResponse>> searchUsers(
            UserSearchRequest request,
            @PageableDefault Pageable pageable) {
        
        log.info("Admin request to search users dynamically with pagination");
        Page<UserResponse> response = userService.searchUsers(request, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        log.info("Admin request to get user details for user ID: {}", id);
        UserResponse response = userService.getUser(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody AdminUserCreateRequest request) {
        log.info("Admin request to create a new user");
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request) {
        log.info("Admin request to update user details for user ID: {}", id);
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<UserResponse> updatePassword(
            @PathVariable Long id,
            @Valid @RequestBody AdminPasswordUpdateRequest request) {
        log.info("Admin request to update password for user ID: {}", id);
        UserResponse response = userService.updateUserPassword(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/lock")
    public ResponseEntity<UserResponse> toggleLock(@PathVariable Long id) {
        log.info("Admin request to toggle lock for user ID: {}", id);
        UserResponse response = userService.toggleUserLock(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Admin request to delete user ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
