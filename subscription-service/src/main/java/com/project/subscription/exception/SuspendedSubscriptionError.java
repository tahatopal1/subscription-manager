package com.project.subscription.exception;

/**
 * Structured 409 response returned when the user has a SUSPENDED subscription.
 * Provides enough context for the frontend to display the correct UI and link
 * the user to the POST /api/subscriptions/{subscriptionId}/retry-payment endpoint.
 */
public record SuspendedSubscriptionError(
        String error,
        String message,
        Long subscriptionId
) {
    public static SuspendedSubscriptionError of(Long subscriptionId, String message) {
        return new SuspendedSubscriptionError("SUBSCRIPTION_SUSPENDED", message, subscriptionId);
    }
}
