package com.project.notification.job;

import com.project.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Trigger Vector 3 — Reliability layer.
 *
 * Runs every 60 seconds and re-attempts the top 50 FAILED notifications
 * that have not exceeded 3 retries.
 *
 * The actual retry logic (increment retryCount, set source=SCHEDULED_RETRY,
 * dispatch, update status) lives in NotificationService — this class is
 * a pure scheduling trigger.
 */
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
