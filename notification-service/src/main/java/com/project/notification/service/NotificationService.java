package com.project.notification.service;

import com.project.notification.dto.event.NotificationEvent;
import com.project.notification.dto.request.NotificationSearchRequest;
import com.project.notification.dto.request.SendCustomNotificationRequest;
import com.project.notification.dto.response.NotificationResponse;
import com.project.notification.entity.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void processEventNotification(NotificationEvent event);

    NotificationResponse sendCustom(SendCustomNotificationRequest request);

    NotificationResponse resend(Long notificationId);

    void retryFailed(int batchSize, int maxRetries);

    Page<NotificationResponse> searchNotifications(NotificationSearchRequest criteria, Pageable pageable);
}
