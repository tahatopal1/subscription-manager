package com.project.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Dead-letter store for payment outbox messages that exhausted all retry attempts.
 *
 * When a {@link PaymentOutboxMessage} reaches the retry limit, the outbox processor:
 *   1. Inserts a copy of the full record here (for diagnostics / manual replay).
 *   2. Deletes the original from the main outbox table (keeping it lean and fast).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "dead_letter_outbox",
    indexes = {
        @Index(name = "idx_dl_payment_outbox_aggregate_id", columnList = "aggregate_id")
    }
)
public class DeadLetterPaymentOutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /** Original outbox message ID — preserved for traceability. */
    @Column(name = "original_outbox_id", nullable = false)
    private Long originalOutboxId;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    /** Total attempts made before giving up. */
    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    /** Timestamp of the last failed attempt. */
    @Column(name = "last_attempt_time")
    private Instant lastAttemptTime;

    /** Exception message from the final failed attempt. */
    @Lob
    @Column(name = "error_reason", columnDefinition = "TEXT")
    private String errorReason;

    /** When this record was moved to the dead-letter table. */
    @CreationTimestamp
    @Column(name = "dead_lettered_at", nullable = false, updatable = false)
    private Instant deadLetteredAt;
}
