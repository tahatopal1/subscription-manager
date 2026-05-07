package com.project.auth.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class AdminUserUpdateRequest {

    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a valid email address")
    private String email;

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotBlank(message = "surname must not be blank")
    private String surname;

    @NotEmpty(message = "roles collection cannot be empty")
    private Set<String> roles;

}
