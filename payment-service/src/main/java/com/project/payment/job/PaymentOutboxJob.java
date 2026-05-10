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

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PaymentOutboxJob {

    static final int MAX_RETRY_COUNT = 3;

    private static final int BATCH_SIZE = 50;

    private static final long CONFIRM_TIMEOUT_SECONDS = 2;

    private final PaymentOutboxMessageRepository           outboxRepository;
    private final DeadLetterPaymentOutboxMessageRepository deadLetterRepository;
    private final RabbitTemplate                           rabbitTemplate;

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
    }


    private void publishSync(PaymentOutboxMessage message) {
        CorrelationData correlationData = new CorrelationData(String.valueOf(message.getId()));

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
    }


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
