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

/**
 * Repository for Subscription persistence.
 * All user-owned queries filter by userId at the SQL level for tenant isolation.
 */
public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {

    Optional<Subscription> findByIdAndUserId(Long id, Long userId);

    List<Subscription> findAllByUserId(Long userId);

    Optional<Subscription> findFirstByUserIdAndStatus(Long userId, SubscriptionStatus status);

    /**
     * Returns true if the user has any subscription in one of the given living states
     * (PENDING, ACTIVE, SUSPENDED). Used to enforce the one-active-subscription-per-user rule.
     */
    boolean existsByUserIdAndStatusIn(Long userId, List<SubscriptionStatus> statuses);

    /**
     * Locks a batch of ACTIVE subscriptions expiring within the renewal window using SKIP LOCKED.
     * Safe for multi-pod deployments: pods skip rows already locked by another instance.
     * {@code cancelAtPeriodEnd = false} excludes subscriptions the user has already cancelled.
     */
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

    /**
     * Locks a batch of ACTIVE subscriptions flagged for end-of-period cancellation
     * whose billing period has already expired, using SKIP LOCKED.
     * Safe for multi-pod deployments: pods skip rows already locked by another instance.
     */
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
