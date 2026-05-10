package com.project.subscription.listener;

import com.project.subscription.config.RabbitMqConfig;
import com.project.subscription.dto.event.PaymentResultEvent;
import com.project.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final SubscriptionService subscriptionService;

    @RabbitListener(queues = RabbitMqConfig.PAYMENT_RESULT_QUEUE)
    public void onPaymentResult(PaymentResultEvent event) {
        log.info("Received payment result: subscriptionId={}, status={}",
                event.subscriptionId(), event.status());
        try {
            subscriptionService.handlePaymentResult(event.subscriptionId(), event.status());
        } catch (Exception e) {
            log.error("Error processing payment result for subscriptionId={}: {}",
                    event.subscriptionId(), e.getMessage(), e);
            throw e;
        }
    }
}
