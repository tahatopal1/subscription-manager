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

public interface PaymentOutboxMessageRepository extends JpaRepository<PaymentOutboxMessage, Long> {

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
