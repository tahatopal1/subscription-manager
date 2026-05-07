package com.project.auth.repository;

import com.project.auth.dto.request.admin.UserSearchRequest;
import com.project.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {

    Page<User> searchUsers(UserSearchRequest request, Pageable pageable);
}
