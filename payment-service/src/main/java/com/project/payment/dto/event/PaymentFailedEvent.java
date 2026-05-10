package com.project.payment.dto.event;

import java.math.BigDecimal;

public record PaymentFailedEvent(
    String transactionId,
    String userId,
    BigDecimal amount,
    String reason
) {}
