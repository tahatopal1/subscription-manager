package com.project.notification.controller;

import com.project.notification.dto.request.NotificationSearchRequest;
import com.project.notification.dto.request.SendCustomNotificationRequest;
import com.project.notification.dto.response.NotificationResponse;
import com.project.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Trigger Vector 2 — Admin REST API.
 *
 * Secured with @PreAuthorize so only callers whose X-User-Roles header
 * contains ROLE_ADMIN (injected by the API Gateway) can access these endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/admin/notifications
     * Paginated search across all notifications with optional filters.
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> searchNotifications(
            NotificationSearchRequest criteria,
            @PageableDefault Pageable pageable) {
        log.info("Admin GET /api/admin/notifications — criteria={}", criteria);
        return ResponseEntity.ok(notificationService.searchNotifications(criteria, pageable));
    }

    /**
     * POST /api/admin/notifications/send-custom
     * Bypass the event queue and send a one-off message to any user.
     */
    @PostMapping("/send-custom")
    public ResponseEntity<NotificationResponse> sendCustom(
            @Valid @RequestBody SendCustomNotificationRequest request) {
        log.info("POST /api/admin/notifications/send-custom — userId={}",
                request.userId());
        return ResponseEntity.ok(notificationService.sendCustom(request));
    }

    /**
     * POST /api/admin/notifications/{id}/resend
     * Re-attempt delivery for an existing notification (FAILED or customer complaint).
     */
    @PostMapping("/{id}/resend")
    public ResponseEntity<NotificationResponse> resend(@PathVariable Long id) {
        log.info("POST /api/admin/notifications/{}/resend", id);
        return ResponseEntity.ok(notificationService.resend(id));
    }
}
