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

/**
 * Contract for user operations including admin management and self-management.
 */
public interface UserService {

    /** Retrieves the profile of the currently authenticated user. */
    UserResponse getProfile(Long userId);

    /** Retrieves a specific user by their ID. */
    UserResponse getUser(Long userId);

    /** Updates the profile of the currently authenticated user. */
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    /** Renews the password for the currently authenticated user. */
    void renewPassword(Long userId, UserPasswordUpdateRequest request);

    /** Searches users dynamically and returns paginated results. */
    Page<UserResponse> searchUsers(UserSearchRequest request, Pageable pageable);

    /** Creates a new user by an admin. */
    UserResponse createUser(AdminUserCreateRequest request);

    /** Replaces a user's entire entity details (email, names, roles, etc). */
    UserResponse updateUser(Long userId, AdminUserUpdateRequest request);

    /** Updates the password for a user. */
    UserResponse updateUserPassword(Long userId, AdminPasswordUpdateRequest request);

    /**
     * Toggles the isLocked flag on a user account.
     * Locking prevents the user from obtaining new JWTs.
     */
    UserResponse toggleUserLock(Long userId);

    /** Hard-deletes a user for GDPR compliance. */
    void deleteUser(Long userId);
}
