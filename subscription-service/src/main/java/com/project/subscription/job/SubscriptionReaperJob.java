package com.project.subscription.job;

import com.project.subscription.entity.Subscription;
import com.project.subscription.entity.enums.SubscriptionStatus;
import com.project.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Nightly reaper job for the "Cancel at Period End" pattern.
 *
 * <p>Finds a batch of ACTIVE subscriptions where:
 * <ul>
 *   <li>{@code cancelAtPeriodEnd = true}</li>
 *   <li>{@code endDate} has already passed</li>
 * </ul>
 * For each, it flips status to CANCELLED. No outbox event is written —
 * there is no downstream consumer for a termination event at this time.
 *
 * <p><b>SKIP LOCKED</b>: the query locks only the rows it will process and skips
 * any rows held by another pod. This makes the job safe to run on multiple
 * instances simultaneously without a distributed lock.
 *
 * <p>Hibernate dirty-checking automatically issues the UPDATE statements at
 * commit time — no explicit {@code save()} calls needed in the loop.
 *
 * <p>Runs daily at midnight.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionReaperJob {

    /** Rows processed per scheduler tick — keeps memory bounded. */
    private static final int BATCH_SIZE = 100;

    private final SubscriptionRepository subscriptionRepository;

//    @Scheduled(cron = "0 0 0 * * *") this is deactivated for testing purposes
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processExpiredCancellations() {
        List<Subscription> expired = subscriptionRepository
                .findExpiredCancellationsForUpdate(Instant.now(), BATCH_SIZE);

        if (expired.isEmpty()) {
            log.info("SubscriptionReaperJob: no expired cancellations to process");
            return;
        }

        log.info("SubscriptionReaperJob: terminating {} subscription(s) past their period end", expired.size());

        for (Subscription subscription : expired) {
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            log.info("SubscriptionReaperJob: terminated subscriptionId={}, userId={}",
                    subscription.getId(), subscription.getUserId());
        }
        // Hibernate dirty-checking flushes all status mutations in a single batch at commit.
    }
}
