package com.project.notification.dto.response;

import com.project.notification.entity.Notification;
import com.project.notification.entity.enums.NotificationStatus;

import java.time.Instant;

public record NotificationResponse(
        Long               id,
        Long               userId,
        Long               eventId,
        String             source,
        String             channel,
        NotificationStatus status,
        String             messageTemplate,
        Integer            retryCount,
        Instant            sentAt,
        Instant            createdAt,
        Instant            updatedAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getUserId(),
                n.getEventId(),
                n.getSource(),
                n.getChannel(),
                n.getStatus(),
                n.getMessageTemplate(),
                n.getRetryCount(),
                n.getSentAt(),
                n.getCreatedAt(),
                n.getUpdatedAt()
        );
    }
}
