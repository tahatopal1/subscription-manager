package com.project.payment.controller;

import com.project.payment.dto.request.AddPaymentMethodRequest;
import com.project.payment.dto.response.PaymentMethodResponse;
import com.project.payment.dto.response.TransactionReceiptResponse;
import com.project.payment.security.CustomUserDetails;
import com.project.payment.service.PaymentMethodService;
import com.project.payment.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class PaymentController {

    private final PaymentMethodService paymentMethodService;
    private final TransactionService   transactionService;

    @GetMapping("/methods")
    public ResponseEntity<List<PaymentMethodResponse>> getMethods(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(paymentMethodService.getMethods(currentUser.getId()));
    }

    @PostMapping("/methods")
    public ResponseEntity<PaymentMethodResponse> addMethod(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody AddPaymentMethodRequest request) {
        return ResponseEntity.ok(paymentMethodService.addMethod(currentUser.getId(), request));
    }

    @DeleteMapping("/methods/{methodId}")
    public ResponseEntity<Void> deleteMethod(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long methodId) {
        paymentMethodService.deleteMethod(currentUser.getId(), methodId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/methods/{methodId}/default")
    public ResponseEntity<PaymentMethodResponse> setDefaultMethod(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long methodId) {
        log.info("PATCH /api/payments/methods/{}/default - userId={}", methodId, currentUser.getId());
        return ResponseEntity.ok(paymentMethodService.setDefaultMethod(currentUser.getId(), methodId));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionReceiptResponse>> getTransactions(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(transactionService.getTransactions(currentUser.getId(), pageable));
    }
}
