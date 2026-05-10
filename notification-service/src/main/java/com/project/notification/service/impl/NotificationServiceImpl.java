package com.project.notification.service.impl;

import com.project.notification.dto.event.NotificationEvent;
import com.project.notification.dto.request.NotificationSearchRequest;
import com.project.notification.dto.request.SendCustomNotificationRequest;
import com.project.notification.dto.response.NotificationResponse;
import com.project.notification.entity.Notification;
import com.project.notification.entity.enums.NotificationStatus;
import com.project.notification.exception.NotFoundException;
import com.project.notification.exception.ProviderUnavailableException;
import com.project.notification.provider.NotificationProvider;
import com.project.notification.repository.NotificationRepository;
import com.project.notification.repository.NotificationSpecification;
import com.project.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationProvider notificationProvider;
  private final RedissonClient redissonClient;

  private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:notification:";
  private static final long IDEMPOTENCY_TTL_DAYS = 15;

  // ── Trigger Vector 1: Async Event ──────────────────────────────────────

  @Override
  @Transactional
  public void processEventNotification(NotificationEvent event) {

    // Redisson idempotency guard — if this key was already processed, skip silently.
    RBucket<Boolean> idempotencyBucket = redissonClient
        .getBucket(
            String.format("%s-%s-%s-%s", IDEMPOTENCY_KEY_PREFIX, event.eventId(), event.userId(),
                event.channel()));
    if (!idempotencyBucket.trySet(true, IDEMPOTENCY_TTL_DAYS, TimeUnit.SECONDS)) {
      log.warn("⚠️  Duplicate eventId={} for userId={} detected via Redis — skipping",
          event.eventId(), event.userId());
      return;
    }

    log.info("📨 Processing event notification: eventId={}, userId={}, channel={}",
        event.eventId(), event.userId(), event.channel());

    Notification notification = Notification.builder()
        .userId(event.userId())
        .eventId(event.eventId())
        .source(event.source())
        .channel(event.channel())
        .status(NotificationStatus.PENDING)
        .messageTemplate(event.message())
        .build();

    notification = notificationRepository.save(notification);
    log.info("💾 Notification persisted as PENDING: id={}", notification.getId());

    dispatch(notification);
  }

  // ── Trigger Vector 2a: Admin — Custom Send ─────────────────────────────

  @Override
  @Transactional
  public NotificationResponse sendCustom(SendCustomNotificationRequest request) {
    log.info("🛠️  Admin custom send: userId={}, channel={}", request.userId(), request.channel());

    Notification notification = Notification.builder()
        .userId(Long.valueOf(request.userId()))
        .eventId(null)           // no upstream event — admin-initiated
        .channel(request.channel())
        .status(NotificationStatus.PENDING)
        .messageTemplate(request.message())
        .build();

    notification = notificationRepository.save(notification);
    dispatch(notification);

    return NotificationResponse.from(notification);
  }

  // ── Trigger Vector 2b: Admin — Resend ─────────────────────────────────

  @Override
  @Transactional
  public NotificationResponse resend(Long notificationId) {
    log.info("🔁 Admin resend requested for notificationId={}", notificationId);

    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new NotFoundException(
            "Notification not found: id=" + notificationId));

    log.info("📨 Resending notification id={} for userId={}, channel={}",
        notification.getId(), notification.getUserId(), notification.getChannel());

    dispatch(notification);

    return NotificationResponse.from(notification);
  }

  // ── Trigger Vector 3: Scheduled Retry ─────────────────────────────────

  @Override
  @Transactional
  public void retryFailed(int batchSize, int maxRetries) {
    List<Notification> candidates = notificationRepository.findRetryEligible(
        NotificationStatus.FAILED, maxRetries, PageRequest.of(0, batchSize));

    if (candidates.isEmpty()) {
      log.debug("🔍 NotificationRetryScheduler: no eligible FAILED notifications to retry");
      return;
    }

    log.info("🔄 NotificationRetryScheduler: retrying {} notification(s)", candidates.size());

    for (Notification notification : candidates) {
      notification.setRetryCount(notification.getRetryCount() + 1);
      log.info("🔄 Retry attempt #{} for notificationId={}, userId={}",
          notification.getRetryCount(), notification.getId(), notification.getUserId());
      dispatch(notification);
    }
  }

  // ── Shared Dispatch Helper ─────────────────────────────────────────────

  /**
   * Calls the provider and updates status in-place. This method is intentionally not @Transactional
   * itself — it always runs within the calling method's transaction so entity state changes are
   * flushed together.
   */
  private void dispatch(Notification notification) {
    try {
      notificationProvider.send(
          String.valueOf(notification.getUserId()),
          notification.getMessageTemplate());

      notification.setStatus(NotificationStatus.SENT);
      notification.setSentAt(Instant.now());
      notificationRepository.save(notification);
      log.info("✅ Notification SENT: id={}, userId={}, channel={}",
          notification.getId(), notification.getUserId(), notification.getChannel());

    } catch (ProviderUnavailableException e) {
      notification.setStatus(NotificationStatus.FAILED);
      notificationRepository.save(notification);
      log.warn("❌ Notification FAILED (provider unavailable): id={}, reason={}",
          notification.getId(), e.getMessage());
    }
  }

  @Override
  public Page<NotificationResponse> searchNotifications(NotificationSearchRequest criteria, Pageable pageable) {
    log.info("Admin searching notifications with criteria={}", criteria);
    return notificationRepository
        .findAll(NotificationSpecification.from(criteria), pageable)
        .map(NotificationResponse::from);
  }
}
