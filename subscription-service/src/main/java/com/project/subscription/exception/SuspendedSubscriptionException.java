package com.project.subscription.exception;

/**
 * Thrown when a user attempts to create a new subscription while they have one in SUSPENDED state.
 * SUSPENDED means they have an unpaid balance — they must fix their payment method, not bypass it.
 * Mapped to HTTP 409 Conflict with a structured payload by GlobalExceptionHandler.
 */
public class SuspendedSubscriptionException extends RuntimeException {

    private final Long subscriptionId;

    public SuspendedSubscriptionException(Long subscriptionId) {
        super("You have an unpaid balance. Please update your payment method to reactivate your subscription.");
        this.subscriptionId = subscriptionId;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }
}
