package com.project.subscription.repository;

import com.project.subscription.entity.Subscription;
import com.project.subscription.entity.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {

    Optional<Subscription> findByIdAndUserId(Long id, Long userId);

    List<Subscription> findAllByUserId(Long userId);

    Optional<Subscription> findFirstByUserIdAndStatus(Long userId, SubscriptionStatus status);

    boolean existsByUserIdAndStatusIn(Long userId, List<SubscriptionStatus> statuses);

    @Query(value = """
            SELECT * FROM subscriptions
            WHERE status = 'ACTIVE'
              AND cancel_at_period_end = false
              AND end_date <= :threshold
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Subscription> findExpiringForUpdate(
            @Param("threshold") Instant threshold,
            @Param("batchSize") int batchSize
    );

    @Query(value = """
            SELECT * FROM subscriptions
            WHERE status = 'ACTIVE'
              AND cancel_at_period_end = true
              AND end_date < :now
            LIMIT :batchSize
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<Subscription> findExpiredCancellationsForUpdate(
            @Param("now") Instant now,
            @Param("batchSize") int batchSize
    );
}
