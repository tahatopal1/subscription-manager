package com.project.notification.job;

import com.project.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

    private static final int BATCH_SIZE  = 50;
    private static final int MAX_RETRIES = 3;

    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 600_000)
    public void retryFailedNotifications() {
        log.info("⏰ NotificationRetryScheduler: starting retry sweep (batchSize={}, maxRetries={})",
                BATCH_SIZE, MAX_RETRIES);
        notificationService.retryFailed(BATCH_SIZE, MAX_RETRIES);
    }
}
