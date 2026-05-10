package com.project.notification.dto.event;

/**
 * Universal inbound event for the notification-service.
 *
 * Any upstream service (subscription, payment, or future domains) publishes
 * this same structure to the notification queue. The notification-service
 * treats it as an opaque delivery contract — it does not interpret the domain
 * or compose any message content.
 *
 * @param eventId  unique idempotency handle (used to deduplicate retries)
 * @param userId   the target account holder
 * @param message  the pre-rendered notification body, owned by the sender
 * @param source   the originating service (e.g. "SUBSCRIPTION", "PAYMENT")
 * @param channel  the specific event channel within that service (e.g. "SUBSCRIPTION.CREATED")
 */
public record NotificationEvent(
        Long eventId,
        Long userId,
        String message,
        String source,
        String channel
) {}
