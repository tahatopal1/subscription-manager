package com.project.notification.listener;

import com.project.notification.config.RabbitMqConfig;
import com.project.notification.dto.event.NotificationEvent;
import com.project.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

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
