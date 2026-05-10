package com.project.payment.controller;

import com.project.payment.dto.request.TransactionSearchCriteria;
import com.project.payment.dto.response.TransactionReceiptResponse;
import com.project.payment.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final TransactionService transactionService;

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionReceiptResponse>> searchTransactions(
            TransactionSearchCriteria criteria,
            @PageableDefault Pageable pageable) {
        log.info("Admin searching transactions with criteria={}", criteria);
        return ResponseEntity.ok(transactionService.searchTransactions(criteria, pageable));
    }
}
