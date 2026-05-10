package com.project.payment.listener;

import com.project.payment.config.RabbitMqConfig;
import com.project.payment.dto.event.SubscriptionEvent;
import com.project.payment.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionEventListener {

    private final TransactionService transactionService;

    @RabbitListener(queues = RabbitMqConfig.SUBSCRIPTION_INITIATED_QUEUE)
    public void handleSubscriptionInitiated(SubscriptionEvent event) {
        log.info("Received SubscriptionInitiatedEvent: subscriptionId={}, userId={}",
                event.subscriptionId(), event.userId());
        try {
            transactionService.processCharge(event.userId(), event.subscriptionId());
        } catch (Exception e) {
            log.error("Failed to process charge for subscriptionId={}", event.subscriptionId(), e);
        }
    }
}
