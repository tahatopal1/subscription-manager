package com.project.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Transactional Outbox table entry for the payment-service.
 *
 * Written atomically with the domain entity update in the same @Transactional boundary.
 * A @Scheduled processor reads unprocessed rows and publishes them to RabbitMQ.
 *
 * Resilience fields:
 * - retryCount      — incremented on each failed publish attempt; capped at MAX_RETRY_COUNT.
 * - lastAttemptTime — timestamp of the most recent publish attempt.
 * - errorReason     — last exception message, stored for diagnostics in the dead-letter table.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "payment_outbox_messages",
    indexes = {
        @Index(name = "idx_outbox_processed", columnList = "processed"),
        @Index(name = "idx_outbox_retry",     columnList = "processed, retry_count")
    }
)
public class PaymentOutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(nullable = false, length = 36)
    private Long aggregateId;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean processed = false;

    /** Number of failed publish attempts. Rows with retryCount >= MAX_RETRY_COUNT are dead-lettered. */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private int retryCount = 0;

    /** Timestamp of the last publish attempt, successful or not. */
    @Column(name = "last_attempt_time")
    private Instant lastAttemptTime;

    /** Last exception message — populated on failure, stored permanently in dead_letter_outbox. */
    @Lob
    @Column(name = "error_reason", columnDefinition = "TEXT")
    private String errorReason;
}
