package com.project.subscription.job;

import com.project.subscription.entity.Subscription;
import com.project.subscription.entity.enums.SubscriptionStatus;
import com.project.subscription.repository.SubscriptionRepository;
import com.project.subscription.service.impl.SubscriptionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Nightly job that finds ACTIVE subscriptions expiring within the next 24 hours
 * and writes a SUBSCRIPTION_RENEWAL_REQUESTED outbox event for each.
 *
 * <p>The Payment Service receives these events and charges the card on file.
 *
 * <p><b>SKIP LOCKED</b>: the query locks only the rows it will process and skips
 * any rows held by another pod. This makes the job safe to run on multiple
 * instances simultaneously without a distributed lock.
 *
 * <p>{@code createRenewalOutboxEvent} carries {@code @Transactional(REQUIRED)} so it
 * joins the outer transaction opened here — the outbox row and any subscription
 * field changes are committed atomically in a single transaction at the end of
 * the method.
 *
 * <p>Runs at 02:00 AM every day.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionRenewalJob {

    /** Rows processed per scheduler tick — keeps memory bounded. */
    private static final int BATCH_SIZE = 100;

    private final SubscriptionRepository    subscriptionRepository;
    private final SubscriptionServiceImpl   subscriptionService; // concrete type for package-private helper

//    @Scheduled(cron = "0 0 2 * * ?")
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processRenewals() {
        Instant threshold = Instant.now().plus(24, ChronoUnit.HOURS);
        List<Subscription> expiring = subscriptionRepository
                .findExpiringForUpdate(threshold, BATCH_SIZE);

        if (expiring.isEmpty()) {
            log.info("SubscriptionRenewalJob: no subscriptions expiring within 24 hours");
            return;
        }

        log.info("SubscriptionRenewalJob: processing {} expiring subscription(s)", expiring.size());

        for (Subscription subscription : expiring) {
            // createRenewalOutboxEvent joins this transaction (REQUIRED propagation).
            // Any exception here rolls back the entire batch, leaving all rows unlocked
            // for the next scheduler tick to retry — safe and consistent.
            subscriptionService.createRenewalOutboxEvent(subscription);
            log.info("SubscriptionRenewalJob: renewal event written for subscriptionId={}, userId={}",
                    subscription.getId(), subscription.getUserId());
        }
    }
}
