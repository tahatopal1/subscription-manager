package com.project.subscription.event;

import com.project.subscription.dto.response.SubscriptionResponse;

public record SubscriptionNotificationEvent(
        SubscriptionResponse response,
        String               channel
) {}
