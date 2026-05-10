package com.project.subscription.dto.response;

import com.project.subscription.entity.Subscription;
import com.project.subscription.entity.enums.SubscriptionStatus;

import java.time.Instant;

public record SubscriptionResponse(
        Long               id,
        Long               userId,
        SubscriptionStatus status,
        boolean            cancelAtPeriodEnd,
        Instant            startDate,
        Instant            endDate,
        Instant            createdAt,
        Instant            updatedAt
) {
    public static SubscriptionResponse from(Subscription s) {
        return new SubscriptionResponse(
                s.getId(),
                s.getUserId(),
                s.getStatus(),
                s.isCancelAtPeriodEnd(),
                s.getStartDate(),
                s.getEndDate(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
