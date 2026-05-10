package com.project.notification.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SendCustomNotificationRequest(
        @NotBlank String userId,
        @NotBlank String message,
        @NotBlank String channel
) {}
