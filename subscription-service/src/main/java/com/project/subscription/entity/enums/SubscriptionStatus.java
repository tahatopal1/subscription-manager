package com.project.subscription.entity.enums;

/**
 * Lifecycle states for a Subscription.
 *
 * Allowed transitions:
 *   PENDING   -> ACTIVE    (payment success)
 *   PENDING   -> FAILED    (payment failed on initial charge)
 *   ACTIVE    -> CANCELLED (user cancels)
 *   ACTIVE    -> SUSPENDED (payment failed on renewal — grace period)
 *   SUSPENDED -> ACTIVE    (retry payment success)
 *   SUSPENDED -> CANCELLED (retry payment failed)
 */
public enum SubscriptionStatus {
    PENDING,
    ACTIVE,
    FAILED,
    CANCELLED,
    SUSPENDED
}
