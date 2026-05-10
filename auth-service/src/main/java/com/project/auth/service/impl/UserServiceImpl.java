package com.project.auth.service.impl;

import com.project.auth.dto.request.admin.AdminPasswordUpdateRequest;
import com.project.auth.dto.request.admin.AdminUserCreateRequest;
import com.project.auth.dto.request.admin.AdminUserUpdateRequest;
import com.project.auth.dto.request.user.UpdateProfileRequest;
import com.project.auth.dto.request.user.UserPasswordUpdateRequest;
import com.project.auth.dto.request.admin.UserSearchRequest;
import com.project.auth.dto.response.UserResponse;
import com.project.auth.entity.User;
import com.project.auth.exception.BusinessException;
import com.project.auth.exception.NotFoundException;
import com.project.auth.repository.UserRepository;
import com.project.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getProfile(Long userId) {
        log.info("Fetching profile for user ID: {}", userId);
        return UserResponse.from(getUserById(userId));
    }

    @Override
    public UserResponse getUser(Long userId) {
        log.info("Admin fetching details for user ID: {}", userId);
        return UserResponse.from(getUserById(userId));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Updating profile for user ID: {}", userId);
        User user = getUserById(userId);
        user.setName(request.name());
        user.setSurname(request.surname());
        user.setEmail(request.email());
        log.info("Profile updated successfully for user ID: {}", userId);
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public void renewPassword(Long userId, UserPasswordUpdateRequest request) {
        log.info("User renewing password for ID: {}", userId);
        User user = getUserById(userId);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            log.warn("Password renewal failed for user ID {}: old password does not match", userId);
            throw new BusinessException("The current password provided is incorrect.");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password renewed successfully for user ID: {}", userId);
    }

    @Override
    public Page<UserResponse> searchUsers(UserSearchRequest request, Pageable pageable) {
        log.info("Admin searching users dynamically with pagination");
        return userRepository.searchUsers(request, pageable).map(UserResponse::from);
    }

    @Override
    @Transactional
    public UserResponse createUser(AdminUserCreateRequest request) {
        log.info("Admin creating new user with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Admin create failed: Email {} is already registered", request.getEmail());
            throw new BusinessException("Email is already in use.");
        }
        User user = User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .surname(request.getSurname())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(request.getRoles())
                .build();
        user = userRepository.save(user);
        log.info("User created successfully by admin with ID: {}", user.getId());
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, AdminUserUpdateRequest request) {
        log.info("Admin updating details for user ID: {}", userId);
        User user = getUserById(userId);
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            log.warn("Admin update failed: Email {} is already registered", request.getEmail());
            throw new BusinessException("Email is already in use by another account.");
        }
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setRoles(request.getRoles());
        log.info("User details updated successfully for user ID: {}", userId);
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserPassword(Long userId, AdminPasswordUpdateRequest request) {
        log.info("Admin updating password for user ID: {}", userId);
        User user = getUserById(userId);
        user.setPassword(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);
        log.info("User password updated successfully for user ID: {}", userId);
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UserResponse toggleUserLock(Long userId) {
        log.info("Admin toggling lock status for user ID: {}", userId);
        User user = getUserById(userId);
        user.setLocked(!user.isLocked());
        user = userRepository.save(user);
        log.info("User lock status toggled to {} for user ID: {}", user.isLocked(), userId);
        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Admin deleting user ID: {}", userId);
        userRepository.delete(getUserById(userId));
        log.info("User deleted successfully for user ID: {}", userId);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
    }
}
