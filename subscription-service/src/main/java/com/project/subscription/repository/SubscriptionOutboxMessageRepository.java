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

public interface SubscriptionOutboxMessageRepository extends JpaRepository<SubscriptionOutboxMessage, Long> {

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
