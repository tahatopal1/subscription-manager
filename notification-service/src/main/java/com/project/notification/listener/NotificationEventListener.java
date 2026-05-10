package com.project.notification.listener;

import com.project.notification.config.RabbitMqConfig;
import com.project.notification.dto.event.NotificationEvent;
import com.project.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Trigger Vector 1 — Asynchronous RabbitMQ event listener.
 *
 * The notification-service is deliberately domain-agnostic. A single listener
 * on a single queue handles every inbound {@link NotificationEvent}, regardless
 * of which upstream service published it. All delivery decisions (type, message)
 * are owned by the sender and carried inside the event — this class is pure routing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMqConfig.NOTIFICATION_QUEUE)
    public void handle(NotificationEvent event) {
        log.info("📥 Received NotificationEvent: eventId={}, userId={}",
                event.eventId(), event.userId());

        notificationService.processEventNotification(event);
    }
}
