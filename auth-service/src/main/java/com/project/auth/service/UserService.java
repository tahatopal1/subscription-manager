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

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @param userId the UUID of the authenticated user
     * @return the user profile
     */
    UserResponse getProfile(String userId);

    /**
     * Retrieves a specific user by their ID.
     *
     * @param userId the UUID of the user
     * @return the user profile
     */
    UserResponse getUser(String userId);

    /**
     * Updates the profile of the currently authenticated user.
     *
     * @param userId the UUID of the authenticated user
     * @param request the updated fields
     * @return the updated user profile
     */
    UserResponse updateProfile(String userId, UpdateProfileRequest request);

    /**
     * Renews the password for the currently authenticated user.
     *
     * @param userId the UUID of the authenticated user
     * @param request the password renewal payload
     */
    void renewPassword(String userId, UserPasswordUpdateRequest request);

    /**
     * Searches users dynamically and returns paginated results.
     *
     * @param request search filters
     * @param pageable pagination parameters
     * @return paginated user profiles
     */
    Page<UserResponse> searchUsers(UserSearchRequest request, Pageable pageable);

    /**
     * Creates a new user by an admin.
     *
     * @param request the details of the new user
     * @return the created user profile
     */
    UserResponse createUser(AdminUserCreateRequest request);

    /**
     * Replaces a user's entire entity details (email, names, roles, etc).
     *
     * @param userId  target user's UUID
     * @param request new full state of the user
     * @return updated user profile
     */
    UserResponse updateUser(String userId, AdminUserUpdateRequest request);

    /**
     * Updates the password for a user.
     *
     * @param userId target user's UUID
     * @param request the new password
     */
    UserResponse updateUserPassword(String userId, AdminPasswordUpdateRequest request);

    /**
     * Toggles the isLocked flag on a user account.
     * Locking prevents the user from obtaining new JWTs (spec §5).
     *
     * @param userId target user's UUID
     * @return updated user profile
     */
    UserResponse toggleUserLock(String userId);

    /**
     * Hard-deletes a user for GDPR compliance and publishes a UserDeletedEvent to RabbitMQ.
     *
     * @param userId target user's UUID
     */
    void deleteUser(String userId);
}
