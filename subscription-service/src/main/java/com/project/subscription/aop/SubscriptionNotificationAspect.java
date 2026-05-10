package com.project.subscription.aop;

import com.project.subscription.constant.SubscriptionNotificationConstants;
import com.project.subscription.dto.response.SubscriptionResponse;
import com.project.subscription.entity.enums.SubscriptionStatus;
import com.project.subscription.event.SubscriptionNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SubscriptionNotificationAspect {

  private final ApplicationEventPublisher eventPublisher;

  @AfterReturning(
      pointcut = "execution(com.project.subscription.dto.response.SubscriptionResponse " +
          "com.project.subscription.service.impl.SubscriptionServiceImpl.*(..))",
      returning = "response"
  )
  public void afterSubscriptionMethod(SubscriptionResponse response) {
    if (response == null) {
      return;   // idempotent-ignore paths return null
    }

    String channel = resolveChannel(response);

    log.debug("Publishing SubscriptionNotificationEvent: userId={}, channel={}",
        response.userId(), channel);
    eventPublisher.publishEvent(new SubscriptionNotificationEvent(response, channel));
  }


  private String resolveChannel(SubscriptionResponse response) {
    SubscriptionStatus status = response.status();

    if (response.status() == SubscriptionStatus.ACTIVE
        && response.cancelAtPeriodEnd()) {
      return response.endDate() != null
          && response.endDate().isAfter(Instant.now())
          ? SubscriptionNotificationConstants.CHANNEL_CANCELLATION_SCHEDULED
          : SubscriptionNotificationConstants.CHANNEL_CANCELLED;
    }

    return switch (status) {
      case PENDING -> SubscriptionNotificationConstants.CHANNEL_PENDING;
      case ACTIVE -> SubscriptionNotificationConstants.CHANNEL_ACTIVE;
      case FAILED -> SubscriptionNotificationConstants.CHANNEL_FAILED;
      case CANCELLED -> SubscriptionNotificationConstants.CHANNEL_CANCELLED;
      case SUSPENDED -> SubscriptionNotificationConstants.CHANNEL_SUSPENDED;
    };
  }

}
