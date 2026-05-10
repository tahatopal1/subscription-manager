package com.project.subscription.exception;

public record SuspendedSubscriptionError(
        String error,
        String message,
        Long subscriptionId
) {
    public static SuspendedSubscriptionError of(Long subscriptionId, String message) {
        return new SuspendedSubscriptionError("SUBSCRIPTION_SUSPENDED", message, subscriptionId);
    }
}
