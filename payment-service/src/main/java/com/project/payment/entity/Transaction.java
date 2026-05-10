package com.project.payment.entity;

import com.project.payment.entity.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_tx_user_id", columnList = "user_id"),
    @Index(name = "idx_tx_subscription_id", columnList = "subscription_id"),
    @Index(name = "idx_tx_idempotency_key", columnList = "idempotency_key", unique = true)
})
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "payment_method_id")
    private Long paymentMethodId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Column(length = 100)
    private String paymentReference;

    @Column(length = 255)
    private String failureReason;
}
