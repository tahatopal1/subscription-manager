package com.project.payment.dto.response;

import com.project.payment.entity.Transaction;
import com.project.payment.entity.enums.TransactionStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionReceiptResponse(
    Long id,
    Long userId,
    BigDecimal amount,
    TransactionStatus status,
    String failureReason,
    Instant createdAt
) {
    public static TransactionReceiptResponse from(Transaction tx) {
        return new TransactionReceiptResponse(
            tx.getId(),
            null,
            tx.getAmount(),
            tx.getStatus(),
            tx.getFailureReason(),
            tx.getCreatedAt()
        );
    }

    public static TransactionReceiptResponse fromAdmin(Transaction tx) {
        return new TransactionReceiptResponse(
            tx.getId(),
            tx.getUserId(),
            tx.getAmount(),
            tx.getStatus(),
            tx.getFailureReason(),
            tx.getCreatedAt()
        );
    }
}
