package com.project.notification.dto.request;

import com.project.notification.entity.enums.NotificationStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

/**
 * Optional filter criteria for the admin notification search endpoint.
 * All fields are nullable — omitted params are ignored in the dynamic query.
 */
public record NotificationSearchRequest(
        Long              id,
        Long              userId,
        Long              eventId,
        String            source,
        String            channel,
        NotificationStatus status,
        Integer           retryCount,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant           sentAtFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant           sentAtTo
) {}
