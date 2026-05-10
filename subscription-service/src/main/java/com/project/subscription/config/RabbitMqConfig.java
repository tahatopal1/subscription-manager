package com.project.subscription.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String SUBSCRIPTION_INITIATED_QUEUE = "subscription.initiated.queue";
    public static final String PAYMENT_RESULT_QUEUE         = "payment.result.queue";
    public static final String NOTIFICATION_QUEUE           = "notification";



    @Bean
    public Queue subscriptionInitiatedQueue() {
        return QueueBuilder.durable(SUBSCRIPTION_INITIATED_QUEUE).build();
    }

    @Bean
    public Queue paymentResultQueue() {
        return QueueBuilder.durable(PAYMENT_RESULT_QUEUE).build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                org.slf4j.LoggerFactory.getLogger(RabbitMqConfig.class)
                        .warn("Message NACK from broker — will retry. cause={}", cause);
            }
        });
        return template;
    }
}
