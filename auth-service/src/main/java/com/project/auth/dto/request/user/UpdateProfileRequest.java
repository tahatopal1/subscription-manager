package com.project.auth.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(

    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be a valid email address")
    String email,

    @NotBlank(message = "name must not be blank")
    String name,

    @NotBlank(message = "surname must not be blank")
    String surname
) {

}
