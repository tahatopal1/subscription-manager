package com.project.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Transactional Outbox table entry for the subscription-service.
 *
 * Written atomically with the domain entity update in the same @Transactional boundary.
 * A @Scheduled processor reads unprocessed rows and publishes them to RabbitMQ.
 *
 * Resilience fields:
 * - retryCount       — incremented on each failed publish attempt; capped at MAX_RETRY_COUNT.
 * - lastAttemptTime  — timestamp of the most recent publish attempt.
 * - errorReason      — last exception message, stored for diagnostics in the dead-letter table.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "subscription_outbox_messages",
    indexes = {
        @Index(name = "idx_sub_outbox_processed", columnList = "processed"),
        @Index(name = "idx_sub_outbox_retry",     columnList = "processed, retry_count")
    }
)
public class SubscriptionOutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /** ID of the affected aggregate. */
    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    /** Event type string, e.g. "SUBSCRIPTION_INITIATED". */
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    /** JSON serialized event payload. */
    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Flipped to true ONLY after successful RabbitMQ confirmation. */
    @Column(name = "processed", nullable = false)
    @Builder.Default
    private boolean processed = false;

    /** Number of failed publish attempts. Rows with retryCount >= MAX_RETRY_COUNT are dead-lettered. */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private int retryCount = 0;

    /** Timestamp of the last publish attempt, successful or not. */
    @Column(name = "last_attempt_time")
    private Instant lastAttemptTime;

    /** Last exception message — populated on failure, cleared on success; stored permanently in dead-letter. */
    @Lob
    @Column(name = "error_reason", columnDefinition = "TEXT")
    private String errorReason;
}
