package com.project.subscription.repository;

import com.project.subscription.entity.SubscriptionOutboxMessage;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.QueryHint;
import java.util.List;

/**
 * Repository for the Transactional Outbox table.
 *
 * The scheduled processor queries for pending messages using SELECT … FOR UPDATE SKIP LOCKED
 * so that multiple concurrent scheduler instances (in a multi-pod deployment) never pick
 * up the same row twice without needing an external distributed lock.
 */
public interface SubscriptionOutboxMessageRepository extends JpaRepository<SubscriptionOutboxMessage, Long> {

    /**
     * Fetches up to {@code limit} unprocessed rows whose retry count is below the threshold,
     * locking them with SKIP LOCKED so concurrent scheduler pods skip rows already locked
     * by another instance.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")) // -2 = SKIP_LOCKED
    @Query("""
            SELECT m FROM SubscriptionOutboxMessage m
            WHERE m.processed = false
              AND m.retryCount < :maxRetries
            ORDER BY m.createdAt ASC
            LIMIT :limit
            """)
    List<SubscriptionOutboxMessage> findPendingForUpdate(
            @Param("maxRetries") int maxRetries,
            @Param("limit")      int limit);
}
