package com.project.payment.dto.event;

import com.project.payment.entity.enums.SubscriptionEventType;

public record SubscriptionEvent(
        Long subscriptionId,
        Long userId,
        SubscriptionEventType eventType
) {}
