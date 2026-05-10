package com.project.subscription.constant;

/**
 * Notification-related constants owned by the subscription-service.
 *
 * The notification-service is agnostic — it stores whatever strings arrive.
 * This class is the single source of truth for what this service sends.
 */
public final class SubscriptionNotificationConstants {

    /** Source tag stamped on every notification originating from this service. */
    public static final String SOURCE = "SUBSCRIPTION";

    // ── Channels ───────────────────────────────────────────────────────────
    public static final String CHANNEL_PENDING                = SOURCE + ".PENDING";
    public static final String CHANNEL_ACTIVE                 = SOURCE + ".ACTIVE";
    public static final String CHANNEL_CANCELLATION_SCHEDULED = SOURCE + ".CANCELLATION_SCHEDULED";
    public static final String CHANNEL_CANCELLED              = SOURCE + ".CANCELLED";
    public static final String CHANNEL_FAILED                 = SOURCE + ".FAILED";
    public static final String CHANNEL_SUSPENDED              = SOURCE + ".SUSPENDED";

    // ── Messages ───────────────────────────────────────────────────────────
    public static final String MESSAGE_PENDING                = "Your subscription #%s has been initiated and is pending payment confirmation.";
    public static final String MESSAGE_ACTIVE                 = "Great news! Your subscription #%s is now active.";
    public static final String MESSAGE_CANCELLATION_SCHEDULED = "Your subscription #%s has been scheduled for cancellation at the end of the billing period.";
    public static final String MESSAGE_CANCELLED              = "Your subscription #%s has been cancelled.";
    public static final String MESSAGE_FAILED                 = "Your subscription #%s could not be activated due to a failed payment. Please add a valid payment method and try again.";
    public static final String MESSAGE_SUSPENDED              = "Your subscription #%s has been suspended due to a failed payment. Please retry.";
    public static final String MESSAGE_DEFAULT                = "Your subscription #%s has been updated.";

    private SubscriptionNotificationConstants() {}
}
