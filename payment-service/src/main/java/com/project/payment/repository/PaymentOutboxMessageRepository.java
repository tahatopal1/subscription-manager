package com.project.payment.repository;

import com.project.payment.entity.PaymentOutboxMessage;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for the Transactional Outbox table.
 *
 * Uses SELECT … FOR UPDATE SKIP LOCKED so that multiple concurrent scheduler
 * instances (in a multi-pod deployment) never pick up the same row twice
 * without needing an external distributed lock.
 */
public interface PaymentOutboxMessageRepository extends JpaRepository<PaymentOutboxMessage, Long> {

    /**
     * Fetches up to {@code limit} unprocessed rows whose retry count is below the threshold,
     * locking them with SKIP LOCKED so concurrent scheduler pods skip rows already locked
     * by another instance.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")) // -2 = SKIP_LOCKED
    @Query("""
            SELECT m FROM PaymentOutboxMessage m
            WHERE m.processed = false
              AND m.retryCount < :maxRetries
            ORDER BY m.createdAt ASC
            LIMIT :limit
            """)
    List<PaymentOutboxMessage> findPendingForUpdate(
            @Param("maxRetries") int maxRetries,
            @Param("limit")      int limit);
}
