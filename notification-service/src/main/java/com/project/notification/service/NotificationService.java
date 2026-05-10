package com.project.notification.service;

import com.project.notification.dto.event.NotificationEvent;
import com.project.notification.dto.request.NotificationSearchRequest;
import com.project.notification.dto.request.SendCustomNotificationRequest;
import com.project.notification.dto.response.NotificationResponse;
import com.project.notification.entity.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /**
     * Triggered by an async domain event listener.
     * Performs idempotency check on eventId before dispatching.
     * All delivery details are carried by the event itself.
     *
     * @param event the inbound notification event
     */
    void processEventNotification(NotificationEvent event);

    /**
     * Triggered by the admin REST API — bypasses the event queue.
     * Attempts immediate delivery.
     */
    NotificationResponse sendCustom(SendCustomNotificationRequest request);

    /**
     * Triggered by the admin REST API — re-sends an existing notification.
     *
     * @param notificationId existing notification to resend
     */
    NotificationResponse resend(Long notificationId);

    /**
     * Triggered by the retry scheduler — re-attempts top N FAILED notifications.
     *
     * @param batchSize maximum number of notifications to process
     * @param maxRetries eligibility ceiling for retryCount
     */
    void retryFailed(int batchSize, int maxRetries);

    /**
     * Admin paginated search across all notifications with optional filters.
     *
     * @param criteria optional filter criteria (all fields nullable)
     * @param pageable pagination and sort
     * @return matching notifications page
     */
    Page<NotificationResponse> searchNotifications(NotificationSearchRequest criteria, Pageable pageable);
}
