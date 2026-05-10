package com.project.subscription.controller;

import com.project.subscription.dto.response.SubscriptionResponse;
import com.project.subscription.security.CustomUserDetails;
import com.project.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("POST /api/subscriptions - userId={}", currentUser.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(subscriptionService.createSubscription(currentUser.getId()));
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptions(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("GET /api/subscriptions - userId={}", currentUser.getId());
        return ResponseEntity.ok(subscriptionService.getSubscriptionsByUserId(currentUser.getId()));
    }

    @PatchMapping("/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("PATCH /api/subscriptions/cancel - userId={}", currentUser.getId());
        return ResponseEntity.ok(subscriptionService.cancelSubscriptionByUserId(currentUser.getId()));
    }

    @PostMapping("/retry")
    public ResponseEntity<SubscriptionResponse> retryPayment(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("POST /api/subscriptions/retry - userId={}", currentUser.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(subscriptionService.retryPaymentByUserId(currentUser.getId()));
    }

    @PatchMapping("/reactivate")
    public ResponseEntity<SubscriptionResponse> reactivate(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        log.info("PATCH /api/subscriptions/reactivate - userId={}", currentUser.getId());
        return ResponseEntity.ok(subscriptionService.reactivateByUserId(currentUser.getId()));
    }
}
