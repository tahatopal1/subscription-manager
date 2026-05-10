package com.project.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

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

    @Column(name = "original_outbox_id", nullable = false)
    private Long originalOutboxId;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_attempt_time")
    private Instant lastAttemptTime;

    @Lob
    @Column(name = "error_reason", columnDefinition = "TEXT")
    private String errorReason;

    @CreationTimestamp
    @Column(name = "dead_lettered_at", nullable = false, updatable = false)
    private Instant deadLetteredAt;
}
