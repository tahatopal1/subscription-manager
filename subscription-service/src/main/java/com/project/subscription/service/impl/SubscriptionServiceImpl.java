package com.project.subscription.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.subscription.dto.event.SubscriptionEvent;
import com.project.subscription.dto.request.SubscriptionSearchRequest;
import com.project.subscription.dto.request.UpdateSubscriptionRequest;
import com.project.subscription.dto.response.SubscriptionResponse;
import com.project.subscription.entity.SubscriptionOutboxMessage;
import com.project.subscription.entity.Subscription;
import com.project.subscription.entity.enums.PaymentStatus;
import com.project.subscription.entity.enums.SubscriptionEventType;
import com.project.subscription.entity.enums.SubscriptionStatus;
import com.project.subscription.exception.BusinessException;
import com.project.subscription.exception.ConflictException;
import com.project.subscription.exception.NotFoundException;
import com.project.subscription.exception.SuspendedSubscriptionException;
import com.project.subscription.repository.SubscriptionOutboxMessageRepository;
import com.project.subscription.repository.SubscriptionRepository;
import com.project.subscription.repository.SubscriptionSpecification;
import com.project.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionServiceImpl implements SubscriptionService {

    /** Statuses that mean a subscription is still "alive" — cannot create another one. */
    private static final List<SubscriptionStatus> LIVING_STATUSES =
            List.of(SubscriptionStatus.PENDING, SubscriptionStatus.ACTIVE, SubscriptionStatus.SUSPENDED);

    /** Prefix for the Redis key used to deduplicate concurrent createSubscription calls. */
    private static final String CREATE_IDEMPOTENCY_PREFIX = "sub:create:lock:";
    /** How long the deduplication key is held — long enough to cover a full request lifecycle. */
    private static final long   CREATE_IDEMPOTENCY_TTL_SECONDS = 30;

    final SubscriptionRepository subscriptionRepository;
    final SubscriptionOutboxMessageRepository subscriptionOutboxMessageRepository;
    final ObjectMapper objectMapper;
    final RedissonClient redissonClient;


    @Override
    @Transactional
    public SubscriptionResponse createSubscription(Long userId) {
        log.info("Creating subscription for userId={}", userId);

        // Redisson idempotency guard — prevents duplicate concurrent submissions from the same user.
        RBucket<Boolean> idempotencyBucket = redissonClient.getBucket(CREATE_IDEMPOTENCY_PREFIX + userId);
        if (!idempotencyBucket.trySet(true, CREATE_IDEMPOTENCY_TTL_SECONDS, TimeUnit.SECONDS)) {
            log.warn("⚠️  Duplicate createSubscription call detected for userId={} — rejecting", userId);
            throw new ConflictException("A subscription creation is already in progress for your account. Please wait and try again.");
        }

        // A SUSPENDED subscription means the user owes money — they must retry payment, not create a new subscription.
        subscriptionRepository.findFirstByUserIdAndStatus(userId, SubscriptionStatus.SUSPENDED)
                .ifPresent(s -> { throw new SuspendedSubscriptionException(s.getId()); });

        // A PENDING or ACTIVE subscription already exists — can't create another while it's live.
        if (subscriptionRepository.existsByUserIdAndStatusIn(userId,
                List.of(SubscriptionStatus.PENDING, SubscriptionStatus.ACTIVE))) {
            throw new ConflictException(
                    "You already have an active or pending subscription. " +
                    "Cancel it before creating a new one.");
        }

        Subscription subscription = Subscription.builder()
                .userId(userId)
                .status(SubscriptionStatus.PENDING)
                .build();

        subscription = subscriptionRepository.save(subscription);

        SubscriptionEvent event = new SubscriptionEvent(
                subscription.getId(), userId, SubscriptionEventType.SUBSCRIPTION_INITIATED);
        persistOutboxMessage(subscription.getId(), event);

        log.info("Subscription created: id={}, userId={}, status=PENDING", subscription.getId(), userId);
        return SubscriptionResponse.from(subscription);
    }

    @Override
    public List<SubscriptionResponse> getSubscriptionsByUserId(Long userId) {
        log.info("Listing subscriptions for userId={}", userId);
        return subscriptionRepository.findAllByUserId(userId).stream()
                .map(SubscriptionResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public SubscriptionResponse cancelSubscriptionByUserId(Long userId) {
        log.info("Scheduling cancellation at period end for userId={}", userId);

        Subscription subscription = subscriptionRepository
                .findFirstByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(
                        "No ACTIVE subscription found for userId=" + userId));

        if (subscription.isCancelAtPeriodEnd()) {
            throw new BusinessException("Subscription %s is already scheduled for cancellation.".formatted(subscription.getId()));
        }

        subscription.setCancelAtPeriodEnd(true);
        subscription = subscriptionRepository.save(subscription);

        log.info("Subscription id={} scheduled for cancellation at period end (endDate={})", subscription.getId(), subscription.getEndDate());
        return SubscriptionResponse.from(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse cancel(Long subscriptionId) {
        log.info("Admin scheduling cancellation at period end for subscriptionId={}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NotFoundException("Subscription not found: " + subscriptionId));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new BusinessException("Only ACTIVE subscriptions can be cancelled. Current status: " + subscription.getStatus());
        }
        if (subscription.isCancelAtPeriodEnd()) {
            throw new BusinessException("Subscription %s is already scheduled for cancellation.".formatted(subscriptionId));
        }

        subscription.setCancelAtPeriodEnd(true);
        subscription = subscriptionRepository.save(subscription);

        log.info("Admin: subscription id={} scheduled for cancellation at period end", subscriptionId);
        return SubscriptionResponse.from(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse handlePaymentResult(Long subscriptionId, PaymentStatus paymentStatus) {
        log.info("Handling payment result for subscriptionId={}, status={}", subscriptionId, paymentStatus);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NotFoundException("Subscription not found: " + subscriptionId));

        boolean isSuccess = paymentStatus == PaymentStatus.SUCCESS;

        if (isSuccess) {
            if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                log.info("Subscription {} already ACTIVE — idempotent ignore", subscriptionId);
                return null;
            }
            Instant now = Instant.now();
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setStartDate(now);
            subscription.setEndDate(now.plus(30, ChronoUnit.DAYS));
            log.info("Subscription {} ACTIVATED, endDate={}", subscriptionId, subscription.getEndDate());
        } else {
            if (subscription.getStatus() == SubscriptionStatus.FAILED) {
                log.info("Subscription {} already FAILED — idempotent ignore", subscriptionId);
                return null;
            }
            if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                subscription.setStatus(SubscriptionStatus.SUSPENDED);
                log.info("Subscription {} SUSPENDED (failed renewal)", subscriptionId);
            } else {
                subscription.setStatus(SubscriptionStatus.FAILED);
                log.info("Subscription {} FAILED (failed initial payment)", subscriptionId);
            }
        }

        subscriptionRepository.save(subscription);
        return SubscriptionResponse.from(subscription);

    }

    @Override
    @Transactional
    public SubscriptionResponse retryPaymentByUserId(Long userId) {
        log.info("Retry payment requested for userId={}", userId);

        Subscription subscription = subscriptionRepository
                .findFirstByUserIdAndStatus(userId, SubscriptionStatus.PENDING)
                .orElseThrow(() -> new NotFoundException(
                        "No PENDING subscription found for userId=" + userId));

        SubscriptionEvent event = new SubscriptionEvent(
                subscription.getId(), userId, SubscriptionEventType.SUBSCRIPTION_INITIATED);
        persistOutboxMessage(subscription.getId(), event);

        log.info("Retry payment outbox event written for subscriptionId={}", subscription.getId());
        return SubscriptionResponse.from(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse retry(Long subscriptionId) {
        log.info("Admin retry payment for subscriptionId={}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NotFoundException("Subscription not found: " + subscriptionId));

        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            throw new BusinessException("Only PENDING subscriptions can be retried. Current status: " + subscription.getStatus());
        }

        SubscriptionEvent event = new SubscriptionEvent(
                subscription.getId(), subscription.getUserId(), SubscriptionEventType.SUBSCRIPTION_INITIATED);
        persistOutboxMessage(subscription.getId(), event);

        log.info("Admin retry payment outbox event written for subscriptionId={}", subscriptionId);
        return SubscriptionResponse.from(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse reactivateByUserId(Long userId) {
        log.info("Reactivating subscription for userId={}", userId);

        Subscription subscription = subscriptionRepository
                .findFirstByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(
                        "No ACTIVE subscription found for userId=" + userId));

        if (!subscription.isCancelAtPeriodEnd()) {
            throw new BusinessException(
                    "Subscription is not scheduled for cancellation — nothing to reactivate.");
        }

        subscription.setCancelAtPeriodEnd(false);
        subscription = subscriptionRepository.save(subscription);

        log.info("Subscription id={} reactivated — cancellation withdrawn", subscription.getId());
        return SubscriptionResponse.from(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse reactivate(Long subscriptionId) {
        log.info("Admin reactivating subscriptionId={}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NotFoundException("Subscription not found: " + subscriptionId));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new BusinessException("Only ACTIVE subscriptions can be reactivated. Current status: " + subscription.getStatus());
        }
        if (!subscription.isCancelAtPeriodEnd()) {
            throw new BusinessException("Subscription is not scheduled for cancellation — nothing to reactivate.");
        }

        subscription.setCancelAtPeriodEnd(false);
        subscription = subscriptionRepository.save(subscription);

        log.info("Admin: subscription id={} reactivated — cancellation withdrawn", subscriptionId);
        return SubscriptionResponse.from(subscription);
    }

    // ── Search & lookup ───────────────────────────────────────────────────

    @Override
    public Page<SubscriptionResponse> getAllSubscriptions(SubscriptionSearchRequest request, Pageable pageable) {
        log.info("Admin listing subscriptions with filters: {}", request);
        return subscriptionRepository.findAll(SubscriptionSpecification.filterBy(request), pageable)
                .map(SubscriptionResponse::from);
    }

    @Override
    public SubscriptionResponse getSubscriptionById(Long subscriptionId) {
        log.info("Admin fetching subscription id={}", subscriptionId);
        return subscriptionRepository.findById(subscriptionId)
                .map(SubscriptionResponse::from)
                .orElseThrow(() -> new NotFoundException("Subscription not found: " + subscriptionId));
    }

    @Override
    @Transactional
    public SubscriptionResponse updateSubscription(Long subscriptionId, UpdateSubscriptionRequest request) {
        log.info("Admin updating subscription id={} with request={}", subscriptionId, request);

        if (!request.hasAnyField()) {
            throw new BusinessException("At least one of status, startDate, or endDate must be provided.");
        }

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new NotFoundException("Subscription not found: " + subscriptionId));

        if (request.status() != null) {
            log.info("Admin overriding status: {} -> {} for subscriptionId={}",
                    subscription.getStatus(), request.status(), subscriptionId);
            subscription.setStatus(request.status());
        }
        if (request.startDate() != null) {
            subscription.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            subscription.setEndDate(request.endDate());
        }

        // Hibernate dirty-checking persists the changes at commit — no explicit save() needed.
        log.info("Admin subscription update applied for subscriptionId={}", subscriptionId);
        return SubscriptionResponse.from(subscription);
    }

    // ── Internal helpers ────────────────────────────────────────────────────

    /**
     * Writes a SUBSCRIPTION_RENEWAL_REQUESTED SubscriptionEvent to the outbox.
     * Must be called within an active @Transactional boundary.
     */
    @Transactional
    public void createRenewalOutboxEvent(Subscription subscription) {
        SubscriptionEvent event = new SubscriptionEvent(
                subscription.getId(), subscription.getUserId(), SubscriptionEventType.SUBSCRIPTION_RENEWAL_REQUESTED);
        persistOutboxMessage(subscription.getId(), event);
        log.info("Renewal outbox event written for subscriptionId={}", subscription.getId());
    }

    // ---- Internal outbox helper -------------------------------------------

    /**
     * Serializes a SubscriptionEvent and persists it to the subscription_outbox_messages table.
     * The event type is read directly from event.eventType() — no redundant parameter needed.
     * Must always be called within the same @Transactional boundary as the entity save.
     */
    void persistOutboxMessage(Long subscriptionId, SubscriptionEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            SubscriptionOutboxMessage outbox = SubscriptionOutboxMessage.builder()
                    .subscriptionId(subscriptionId)
                    .eventType(event.eventType().name())
                    .payload(json)
                    .processed(false)
                    .build();
            subscriptionOutboxMessageRepository.save(outbox);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox payload for eventType={}", event.eventType(), e);
            throw new BusinessException("Failed to serialize event payload: " + e.getMessage());
        }
    }
}
