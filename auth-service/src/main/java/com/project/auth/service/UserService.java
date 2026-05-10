package com.project.auth.service;

import com.project.auth.dto.request.admin.AdminPasswordUpdateRequest;
import com.project.auth.dto.request.admin.AdminUserCreateRequest;
import com.project.auth.dto.request.admin.AdminUserUpdateRequest;
import com.project.auth.dto.request.user.UpdateProfileRequest;
import com.project.auth.dto.request.user.UserPasswordUpdateRequest;
import com.project.auth.dto.request.admin.UserSearchRequest;
import com.project.auth.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse getProfile(Long userId);

    UserResponse getUser(Long userId);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    void renewPassword(Long userId, UserPasswordUpdateRequest request);

    Page<UserResponse> searchUsers(UserSearchRequest request, Pageable pageable);

    UserResponse createUser(AdminUserCreateRequest request);

    UserResponse updateUser(Long userId, AdminUserUpdateRequest request);

    UserResponse updateUserPassword(Long userId, AdminPasswordUpdateRequest request);

    UserResponse toggleUserLock(Long userId);

    void deleteUser(Long userId);
}
