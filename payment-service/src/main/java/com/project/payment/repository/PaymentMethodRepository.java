package com.project.payment.repository;

import com.project.payment.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    List<PaymentMethod> findAllByUserId(Long userId);
    Optional<PaymentMethod> findByIdAndUserId(Long id, Long userId);
    Optional<PaymentMethod> findFirstByUserId(Long userId);
    boolean existsByUserIdAndIsDefault(Long userId, boolean isDefault);
    boolean existsByGatewayTokenAndUserId(String gatewayToken, Long userId);
}
