package com.project.payment.service;

import com.project.payment.dto.request.AddPaymentMethodRequest;
import com.project.payment.dto.response.PaymentMethodResponse;
import com.project.payment.entity.PaymentMethod;
import com.project.payment.exception.DuplicatePaymentMethodException;
import com.project.payment.exception.NotFoundException;
import com.project.payment.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    public List<PaymentMethodResponse> getMethods(Long userId) {
        log.info("Fetching payment methods for userId={}", userId);
        return paymentMethodRepository.findAllByUserId(userId)
                .stream()
                .map(PaymentMethodResponse::from)
                .toList();
    }

    @Transactional
    public PaymentMethodResponse addMethod(Long userId, AddPaymentMethodRequest request) {
        log.info("Adding payment method for userId={}", userId);

        if (paymentMethodRepository.existsByGatewayTokenAndUserId(request.gatewayToken(), userId)) {
            throw new DuplicatePaymentMethodException(
                    "Payment method with token '" + request.gatewayToken() + "' is already registered for this account.");
        }

        boolean isFirst = !paymentMethodRepository.existsByUserIdAndIsDefault(userId, true);

        PaymentMethod pm = PaymentMethod.builder()
                .userId(userId)
                .gatewayToken(request.gatewayToken())
                .lastFour(request.lastFour())
                .brand(request.brand())
                .isDefault(isFirst || request.isDefault())
                .build();

        if (pm.isDefault()) {
            paymentMethodRepository.findAllByUserId(userId)
                    .forEach(existing -> existing.setDefault(false));
        }

        pm = paymentMethodRepository.save(pm);
        return PaymentMethodResponse.from(pm);
    }

    @Transactional
    public void deleteMethod(Long userId, Long methodId) {
        log.info("Deleting payment method id={} for userId={}", methodId, userId);
        PaymentMethod pm = paymentMethodRepository.findByIdAndUserId(methodId, userId)
                .orElseThrow(() -> new NotFoundException("Payment method not found"));

        boolean wasDefault = pm.isDefault();
        paymentMethodRepository.delete(pm);

        if (wasDefault) {
            paymentMethodRepository.findFirstByUserId(userId).ifPresent(next -> {
                next.setDefault(true);
                paymentMethodRepository.save(next);
                log.info("Promoted payment method id={} as new default for userId={}", next.getId(), userId);
            });
        }
    }

    @Transactional
    public PaymentMethodResponse setDefaultMethod(Long userId, Long methodId) {
        log.info("Setting default payment method id={} for userId={}", methodId, userId);

        PaymentMethod target = paymentMethodRepository.findByIdAndUserId(methodId, userId)
                .orElseThrow(() -> new NotFoundException("Payment method not found"));

        if (target.isDefault()) {
            log.info("Payment method id={} is already the default for userId={}", methodId, userId);
            return PaymentMethodResponse.from(target);
        }

        paymentMethodRepository.findAllByUserId(userId)
                .forEach(pm -> pm.setDefault(false));

        target.setDefault(true);
        log.info("Payment method id={} set as default for userId={}", methodId, userId);
        return PaymentMethodResponse.from(target);
    }

}
