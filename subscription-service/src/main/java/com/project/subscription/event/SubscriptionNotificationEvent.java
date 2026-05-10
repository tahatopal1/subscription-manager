package com.project.subscription.event;

import com.project.subscription.dto.response.SubscriptionResponse;

/**
 * Internal Spring Application Event published by the AOP advice after a
 * subscription-mutating method commits successfully.
 *
 * The {@code channel} field carries the canonical action string
 * (e.g. "SUBSCRIPTION.CREATED") so the listener can build the
 * correct {@code NotificationEvent} without switching on the response state.
 *
 * @param response the committed subscription snapshot
 * @param channel  the canonical notification channel (see NotificationChannel constants)
 */
public record SubscriptionNotificationEvent(
        SubscriptionResponse response,
        String               channel
) {}
