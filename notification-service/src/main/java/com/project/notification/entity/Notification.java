package com.project.notification.entity;

import com.project.notification.entity.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notifications_status",   columnList = "status"),
        @Index(name = "idx_notifications_event_id", columnList = "event_id"),
        @Index(name = "idx_notifications_channel",  columnList = "channel")
    }
)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /** Downstream userId from the event or admin call. Never null. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Unique idempotency handle from the upstream domain event. Null for admin-initiated notifications. */
    @Column(name = "event_id", unique = true)
    private Long eventId;

    /** The originating service that published this notification (e.g. "SUBSCRIPTION", "PAYMENT"). */
    @Column(name = "source", length = 50)
    private String source;

    /** The specific event channel within the source (e.g. "SUBSCRIPTION.CREATED", "PAYMENT.FAILED"). */
    @Column(name = "channel", nullable = false, length = 100)
    private String channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private NotificationStatus status;

    /** The final message that was (or will be) sent to the user. */
    @Column(name = "message_template", nullable = false, columnDefinition = "TEXT")
    private String messageTemplate;

    /** Incremented each time the scheduled retry job re-attempts this notification. */
    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    /** Populated once a successful delivery is confirmed by the provider. */
    @Column(name = "sent_at")
    private Instant sentAt;
}
