package com.project.auth.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserCreateRequest extends AdminUserUpdateRequest {

    @NotBlank(message = "password must not be blank")
    @Size(min = 8, max = 128, message = "password must be between 8 and 128 characters")
    private String password;
}
