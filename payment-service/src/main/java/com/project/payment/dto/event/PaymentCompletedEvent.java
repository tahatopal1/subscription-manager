package com.project.payment.dto.event;

import java.math.BigDecimal;

public record PaymentCompletedEvent(
    String transactionId,
    String userId,
    BigDecimal amount
) {}
