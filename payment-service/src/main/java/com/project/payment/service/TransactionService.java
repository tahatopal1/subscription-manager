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

    /** Fixed subscription charge — replace with plan-service lookup when pricing tiers are introduced. */
    private static final BigDecimal  SUBSCRIPTION_PRICE       = new BigDecimal("29.99");
    /** Idempotency key template: SUB-{subscriptionId}-{billingPeriod}. */
    private static final String      IDEMPOTENCY_KEY_PATTERN  = "SUB-%s-%s";
    /** Billing period granularity — month + 2-digit year, e.g. "05-25". */
    private static final String      BILLING_PERIOD_FORMAT    = "MM-yy";

    /** User-scoped transaction history — filtered by userId. */
    public Page<TransactionReceiptResponse> getTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findAllByUserId(userId, pageable)
                .map(TransactionReceiptResponse::from);
    }

    /** Admin-accessible transaction search — applies dynamic criteria with no userId constraint. */
    public Page<TransactionReceiptResponse> searchTransactions(TransactionSearchCriteria criteria, Pageable pageable) {
        return transactionRepository.findAll(TransactionSpecification.filterBy(criteria), pageable)
                .map(TransactionReceiptResponse::fromAdmin);
    }

    /**
     * Step 3: Called by SubscriptionEventListener when a SUBSCRIPTION_INITIATED message arrives.
     * Processes the payment and atomically persists a PaymentResultEvent to the outbox.
     * Step 4: The OutboxProcessor then delivers that outbox entry to PAYMENT_RESULT_QUEUE.
     *
     * <p>Amount and idempotencyKey are forged here:
     * <ul>
     *   <li><b>amount</b> — fixed MVP price (plan-service lookup in future iterations).</li>
     *   <li><b>idempotencyKey</b> — scoped to subscription + billing period (MM-yy),
     *       preventing double-charges within the same month while allowing charges in future cycles.</li>
     * </ul>
     */
    public void processCharge(Long userId, Long subscriptionId) {

        // ── Forge idempotency key ──────────────────────────────────────────
        // Format: SUB-{subscriptionId}-{MM-yy}
        // Allows re-billing in a new month while deduplicating retries within the same period.
        String billingPeriod  = DateTimeFormatter.ofPattern(BILLING_PERIOD_FORMAT).format(ZonedDateTime.now());
        String idempotencyKey = IDEMPOTENCY_KEY_PATTERN.formatted(subscriptionId, billingPeriod);

        log.info("Processing charge for userId={}, subscriptionId={}, amount={}, idempotencyKey={}",
                userId, subscriptionId, SUBSCRIPTION_PRICE, idempotencyKey);

        // Redisson idempotency guard — if this key was already processed, skip silently.
        RBucket<Boolean> idempotencyBucket = redissonClient.getBucket(IDEMPOTENCY_KEY_PREFIX + idempotencyKey);
        if (!idempotencyBucket.trySet(true, IDEMPOTENCY_TTL_SECONDS, TimeUnit.SECONDS)) {
            log.info("Duplicate idempotencyKey={} detected via Redis — skipping charge", idempotencyKey);
            return;
        }

        // Fetch the user's default payment method
        // Persist transaction as PENDING
        Transaction tx = Transaction.builder()
                .userId(userId)
                .subscriptionId(subscriptionId)
                .amount(SUBSCRIPTION_PRICE)
                .status(TransactionStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .build();
        tx = transactionRepository.save(tx);

        // Resolve the user's default payment method.
        // If none exists, record as FAILED and return immediately — no exception-as-control-flow.
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

        // Charge the gateway — actual I/O failure is the only thing that belongs in a try-catch
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

    // ---- Internal helper -------------------------------------------------------

    /**
     * Writes a PaymentResultEvent row to payment_outbox_messages.
     * Must be called within an active @Transactional boundary.
     */
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
