package com.project.subscription.listener;

import com.project.subscription.config.RabbitMqConfig;
import com.project.subscription.constant.SubscriptionNotificationConstants;
import com.project.subscription.event.SubscriptionNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionNotificationListener {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubscriptionNotification(SubscriptionNotificationEvent appEvent) {
        var response = appEvent.response();
        var channel  = appEvent.channel();

        Map<String, Object> notificationEvent = Map.of(
                "eventId", response.id(),
                "userId",  response.userId(),
                "message", buildMessage(channel, response.id()),
                "source",  SubscriptionNotificationConstants.SOURCE,
                "channel", channel
        );

        rabbitTemplate.convertAndSend(RabbitMqConfig.NOTIFICATION_QUEUE, notificationEvent);

        log.info("📤 Notification dispatched: userId={}, channel={}", response.userId(), channel);
    }


    private String buildMessage(String channel, Object subscriptionId) {
        String template = switch (channel) {
            case SubscriptionNotificationConstants.CHANNEL_PENDING                -> SubscriptionNotificationConstants.MESSAGE_PENDING;
            case SubscriptionNotificationConstants.CHANNEL_ACTIVE                 -> SubscriptionNotificationConstants.MESSAGE_ACTIVE;
            case SubscriptionNotificationConstants.CHANNEL_CANCELLATION_SCHEDULED -> SubscriptionNotificationConstants.MESSAGE_CANCELLATION_SCHEDULED;
            case SubscriptionNotificationConstants.CHANNEL_CANCELLED              -> SubscriptionNotificationConstants.MESSAGE_CANCELLED;
            case SubscriptionNotificationConstants.CHANNEL_FAILED                 -> SubscriptionNotificationConstants.MESSAGE_FAILED;
            case SubscriptionNotificationConstants.CHANNEL_SUSPENDED              -> SubscriptionNotificationConstants.MESSAGE_SUSPENDED;
            default                                                               -> SubscriptionNotificationConstants.MESSAGE_DEFAULT;
        };
        return template.formatted(subscriptionId);
    }

}
