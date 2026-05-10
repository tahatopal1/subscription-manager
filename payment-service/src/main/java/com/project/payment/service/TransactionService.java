package com.project.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.payment.dto.event.PaymentResultEvent;
import com.project.payment.dto.request.TransactionSearchCriteria;
import com.project.payment.dto.response.TransactionReceiptResponse;
import com.project.payment.entity.PaymentOutboxMessage;
import com.project.payment.entity.PaymentMethod;
import com.project.payment.entity.Transaction;
import com.project.payment.entity.enums.PaymentStatus;
import com.project.payment.entity.enums.TransactionStatus;
import com.project.payment.client.PaymentClient;
import com.project.payment.repository.PaymentOutboxMessageRepository;
import com.project.payment.repository.PaymentMethodRepository;
import com.project.payment.repository.TransactionRepository;
import com.project.payment.repository.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
 import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentClient paymentClient;
    private final PaymentOutboxMessageRepository paymentOutboxMessageRepository;
    private final ObjectMapper objectMapper;
    private final RedissonClient redissonClient;

    private static final String     IDEMPOTENCY_KEY_PREFIX   = "idempotency:payment:";
    private static final long IDEMPOTENCY_TTL_SECONDS = 15;

    private static final BigDecimal  SUBSCRIPTION_PRICE       = new BigDecimal("29.99");
    private static final String      IDEMPOTENCY_KEY_PATTERN  = "SUB-%s-%s";
    private static final String      BILLING_PERIOD_FORMAT    = "MM-yy";

    public Page<TransactionReceiptResponse> getTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findAllByUserId(userId, pageable)
                .map(TransactionReceiptResponse::from);
    }

    public Page<TransactionReceiptResponse> searchTransactions(TransactionSearchCriteria criteria, Pageable pageable) {
        return transactionRepository.findAll(TransactionSpecification.filterBy(criteria), pageable)
                .map(TransactionReceiptResponse::fromAdmin);
    }

    public void processCharge(Long userId, Long subscriptionId) {

        String billingPeriod  = DateTimeFormatter.ofPattern(BILLING_PERIOD_FORMAT).format(ZonedDateTime.now());
        String idempotencyKey = IDEMPOTENCY_KEY_PATTERN.formatted(subscriptionId, billingPeriod);

        log.info("Processing charge for userId={}, subscriptionId={}, amount={}, idempotencyKey={}",
                userId, subscriptionId, SUBSCRIPTION_PRICE, idempotencyKey);

        RBucket<Boolean> idempotencyBucket = redissonClient.getBucket(IDEMPOTENCY_KEY_PREFIX + idempotencyKey);
        if (!idempotencyBucket.trySet(true, IDEMPOTENCY_TTL_SECONDS, TimeUnit.SECONDS)) {
            log.info("Duplicate idempotencyKey={} detected via Redis — skipping charge", idempotencyKey);
            return;
        }

        Transaction tx = Transaction.builder()
                .userId(userId)
                .subscriptionId(subscriptionId)
                .amount(SUBSCRIPTION_PRICE)
                .status(TransactionStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .build();
        tx = transactionRepository.save(tx);

        Optional<PaymentMethod> pmOptional = paymentMethodRepository
                .findAllByUserId(userId)
                .stream()
                .filter(PaymentMethod::isDefault)
                .findFirst();

        if (pmOptional.isEmpty()) {
            log.warn("No default payment method for userId={} — recording payment failure", userId);
            tx.setStatus(TransactionStatus.FAILED);
            tx.setFailureReason("No default payment method found");
            transactionRepository.save(tx);
            if (subscriptionId != null) {
                persistPaymentResultOutbox(subscriptionId, PaymentStatus.FAILED);
            }
            return;
        }

        PaymentMethod pm = pmOptional.get();
        tx.setPaymentMethodId(pm.getId());

        PaymentStatus resultStatus;
        try {
            String reference = paymentClient.charge(pm.getGatewayToken(), SUBSCRIPTION_PRICE);
            tx.setStatus(TransactionStatus.COMPLETED);
            tx.setPaymentReference(reference);
            resultStatus = PaymentStatus.SUCCESS;
            log.info("Payment succeeded for tx={} for payment method={}", tx.getId(), pm.getId());
        } catch (Exception e) {
            log.warn("Gateway charge failed for tx={}: for payment method={}, detail={}", tx.getId(), pm.getId(), e.getMessage());
            tx.setStatus(TransactionStatus.FAILED);
            tx.setFailureReason(e.getMessage());
            resultStatus = PaymentStatus.FAILED;
        }

        transactionRepository.save(tx);

        if (subscriptionId != null) {
            persistPaymentResultOutbox(subscriptionId, resultStatus);
        }
    }

    private void persistPaymentResultOutbox(Long subscriptionId, PaymentStatus status) {
        try {
            PaymentResultEvent event = new PaymentResultEvent(subscriptionId, status);
            String json = objectMapper.writeValueAsString(event);
            PaymentOutboxMessage outbox = PaymentOutboxMessage.builder()
                    .aggregateId(subscriptionId)
                    .eventType("PAYMENT_RESULT")
                    .payload(json)
                    .processed(false)
                    .build();
            paymentOutboxMessageRepository.save(outbox);
            log.info("PaymentResult outbox persisted: subscriptionId={}, status={}", subscriptionId, status);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize PaymentResultEvent", e);
        }
    }
}
