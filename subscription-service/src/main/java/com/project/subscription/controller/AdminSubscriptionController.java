package com.project.subscription.controller;

import com.project.subscription.dto.request.SubscriptionSearchRequest;
import com.project.subscription.dto.request.UpdateSubscriptionRequest;
import com.project.subscription.dto.response.SubscriptionResponse;
import com.project.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/admin/subscriptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubscriptionController {

    private final SubscriptionService subscriptionService;
    private final RedisConnectionFactory redisConnectionFactory;

    @GetMapping
    public ResponseEntity<Page<SubscriptionResponse>> getAllSubscriptions(
            SubscriptionSearchRequest request,
            @PageableDefault Pageable pageable) {
        log.info("Admin GET /api/admin/subscriptions");
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions(request, pageable));
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(
            @PathVariable Long subscriptionId) {
        log.info("Admin GET /api/admin/subscriptions/{}", subscriptionId);
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(subscriptionId));
    }

    @PutMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionResponse> updateSubscription(
            @PathVariable Long subscriptionId,
            @Valid @RequestBody UpdateSubscriptionRequest request) {
        log.info("Admin PUT /api/admin/subscriptions/{} request={}", subscriptionId, request);
        return ResponseEntity.ok(subscriptionService.updateSubscription(subscriptionId, request));
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<SubscriptionResponse> createSubscriptionForUser(
            @PathVariable Long userId) {
        log.info("Admin POST /api/admin/subscriptions/users/{}", userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(subscriptionService.createSubscription(userId));
    }

    @PatchMapping("/{subscriptionId}/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @PathVariable Long subscriptionId) {
        log.info("Admin PATCH /api/admin/subscriptions/{}/cancel", subscriptionId);
        return ResponseEntity.ok(subscriptionService.cancel(subscriptionId));
    }

    @PostMapping("/{subscriptionId}/retry")
    public ResponseEntity<SubscriptionResponse> retryPayment(
            @PathVariable Long subscriptionId) {
        log.info("Admin POST /api/admin/subscriptions/{}/retry", subscriptionId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(subscriptionService.retry(subscriptionId));
    }

    @PatchMapping("/{subscriptionId}/reactivate")
    public ResponseEntity<SubscriptionResponse> reactivate(
            @PathVariable Long subscriptionId) {
        log.info("Admin PATCH /api/admin/subscriptions/{}/reactivate", subscriptionId);
        return ResponseEntity.ok(subscriptionService.reactivate(subscriptionId));
    }

    @DeleteMapping("/cache")
    public void flushEntireDatabase() {
        redisConnectionFactory.getConnection().serverCommands().flushDb();
        log.warn("REDIS DATABASE COMPLETELY FLUSHED!");
    }
}
