package com.project.notification.dto.event;

public record NotificationEvent(
        Long eventId,
        Long userId,
        String message,
        String source,
        String channel
) {}
