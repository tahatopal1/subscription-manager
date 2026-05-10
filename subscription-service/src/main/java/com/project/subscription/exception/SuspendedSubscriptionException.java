package com.project.subscription.exception;

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
