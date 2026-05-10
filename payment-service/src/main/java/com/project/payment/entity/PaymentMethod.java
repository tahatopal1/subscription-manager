package com.project.payment.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "payment_methods",
    indexes = {
        @Index(name = "idx_pm_user_id", columnList = "user_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_pm_gateway_token_user_id",
            columnNames = {"gateway_token", "user_id"}
        )
    }
)
public class PaymentMethod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String gatewayToken;

    @Column(nullable = false, length = 4)
    private String lastFour;

    @Column(nullable = false, length = 20)
    private String brand;

    @Column(nullable = false)
    private boolean isDefault;
}
