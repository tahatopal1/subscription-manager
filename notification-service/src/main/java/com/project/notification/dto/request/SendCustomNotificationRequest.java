package com.project.notification.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for POST /api/admin/notifications/send-custom.
 */
public record SendCustomNotificationRequest(
        @NotBlank String userId,
        @NotBlank String message,
        @NotBlank String channel
) {}
