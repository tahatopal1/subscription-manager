package com.project.notification.provider;

/**
 * Contract for notification delivery providers.
 *
 * The notification-service is agnostic to delivery channel — the provider
 * implementation decides how to route the message (email, SMS, push, etc.).
 * Swap implementations without touching any other layer.
 */
public interface NotificationProvider {

    /**
     * Delivers a notification to the given user.
     *
     * @param userId  the target user identifier
     * @param message the pre-rendered message body
     */
    void send(String userId, String message);
}
