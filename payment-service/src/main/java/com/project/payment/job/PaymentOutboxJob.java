package com.project.payment.job;

import com.project.payment.config.RabbitMqConfig;
import com.project.payment.entity.DeadLetterPaymentOutboxMessage;
import com.project.payment.entity.PaymentOutboxMessage;
import com.project.payment.repository.DeadLetterPaymentOutboxMessageRepository;
import com.project.payment.repository.PaymentOutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Transactional Outbox Processor — payment-service, Synchronous Confirm Edition.
 *
 * <p>Every 5 seconds this job:
 * <ol>
 *   <li>Fetches a batch of unprocessed rows using {@code SELECT … FOR UPDATE SKIP LOCKED}
 *       — safe for multi-pod deployments, no distributed lock needed.</li>
 *   <li>For each row: builds the RabbitMQ message, sends it, then <em>blocks</em> waiting
 *       for the broker ACK/NACK (max 2 s). All work happens on the same thread that
 *       holds the DB lock, eliminating the async-callback deadlock.</li>
 *   <li>On ACK  — sets processed = true (dirty-checked by Hibernate on commit).</li>
 *   <li>On NACK / timeout — increments retryCount; dead-letters when limit is reached.</li>
 * </ol>
 *
 * <p><b>Why synchronous?</b> The async-callback pattern (whenComplete → self.markAsProcessed)
 * causes a deadlock under load: the RabbitMQ I/O thread tries to UPDATE the same rows that
 * the scheduler thread still has locked via SKIP LOCKED. Blocking on the confirm here is safe
 * because this is a background worker, not a user-facing thread.
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PaymentOutboxJob {

    /** Maximum publish attempts before a message is dead-lettered. */
    static final int MAX_RETRY_COUNT = 3;

    /** Number of rows fetched per scheduler tick — keeps memory bounded. */
    private static final int BATCH_SIZE = 50;

    /** How long to wait for a broker ACK before treating it as a failure. */
    private static final long CONFIRM_TIMEOUT_SECONDS = 2;

    private final PaymentOutboxMessageRepository           outboxRepository;
    private final DeadLetterPaymentOutboxMessageRepository deadLetterRepository;
    private final RabbitTemplate                           rabbitTemplate;

    // ── Scheduler ─────────────────────────────────────────────────────────

    /**
     * Holds the SKIP LOCKED transaction open for the entire batch.
     * Hibernate dirty-checking flushes all field mutations automatically on commit —
     * no explicit {@code save()} calls required inside the loop.
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutbox() {
        List<PaymentOutboxMessage> pending =
                outboxRepository.findPendingForUpdate(MAX_RETRY_COUNT, BATCH_SIZE);

        if (pending.isEmpty()) return;

        log.debug("OutboxProcessor: found {} pending message(s)", pending.size());

        for (PaymentOutboxMessage message : pending) {
            publishSync(message);
        }
        // Hibernate detects all setXxx() mutations on managed entities and issues
        // the UPDATE statements as part of the commit — no saveAll() needed.
    }

    // ── Synchronous publish ───────────────────────────────────────────────

    private void publishSync(PaymentOutboxMessage message) {
        CorrelationData correlationData = new CorrelationData(String.valueOf(message.getId()));

        // Payload is already serialised JSON — send raw bytes to avoid double-encoding.
        Message rabbitMessage = MessageBuilder
                .withBody(message.getPayload().getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .build();

        message.setLastAttemptTime(Instant.now());

        try {
            rabbitTemplate.send(
                    "",
                    RabbitMqConfig.PAYMENT_RESULT_QUEUE,
                    rabbitMessage,
                    correlationData
            );

            CorrelationData.Confirm confirm =
                    correlationData.getFuture().get(CONFIRM_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (confirm != null && confirm.isAck()) {
                message.setProcessed(true);
                message.setErrorReason(null);
                log.info("Outbox message confirmed by broker: id={}, type={}",
                        message.getId(), message.getEventType());
            } else {
                handleFailure(message, "Broker NACK");
            }

        } catch (Exception e) {
            handleFailure(message, e.getMessage());
        }
    }

    // ── Failure handling ──────────────────────────────────────────────────

    private void handleFailure(PaymentOutboxMessage message, String reason) {
        int newCount = message.getRetryCount() + 1;
        message.setRetryCount(newCount);
        message.setErrorReason(reason);

        if (newCount >= MAX_RETRY_COUNT) {
            log.error("Outbox message id={} exceeded max retries ({}) — moving to dead-letter table. reason={}",
                    message.getId(), MAX_RETRY_COUNT, reason);
            deadLetter(message);
        } else {
            log.warn("Outbox message id={} failed (attempt {}/{}): {}",
                    message.getId(), newCount, MAX_RETRY_COUNT, reason);
        }
        // No save() here either — dirty checking picks up setRetryCount/setErrorReason.
    }

    // ── Dead-letter helper ────────────────────────────────────────────────

    private void deadLetter(PaymentOutboxMessage source) {
        DeadLetterPaymentOutboxMessage dlq = DeadLetterPaymentOutboxMessage.builder()
                .originalOutboxId(source.getId())
                .aggregateId(source.getAggregateId())
                .eventType(source.getEventType())
                .payload(source.getPayload())
                .retryCount(source.getRetryCount())
                .lastAttemptTime(source.getLastAttemptTime())
                .errorReason(source.getErrorReason())
                .build();

        deadLetterRepository.save(dlq);
        outboxRepository.delete(source);

        log.info("Dead-lettered payment outbox message: originalId={}, aggregateId={}, type={}",
                source.getId(), source.getAggregateId(), source.getEventType());
    }
}
