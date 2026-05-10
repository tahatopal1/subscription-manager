package com.project.subscription.dto.request;

import com.project.subscription.entity.enums.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateSubscriptionRequest(

        SubscriptionStatus status,

        Instant startDate,

        Instant endDate
) {
    public boolean hasAnyField() {
        return status != null || startDate != null || endDate != null;
    }
}
