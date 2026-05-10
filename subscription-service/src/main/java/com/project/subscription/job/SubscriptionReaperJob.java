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

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionReaperJob {

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
    }
}
