package com.project.subscription.dto.event;

import com.project.subscription.entity.enums.SubscriptionEventType;

public record SubscriptionEvent(
        Long subscriptionId,
        Long userId,
        SubscriptionEventType eventType
) {}
