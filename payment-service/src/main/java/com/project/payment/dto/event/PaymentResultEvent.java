package com.project.payment.dto.event;

import com.project.payment.entity.enums.PaymentStatus;

public record PaymentResultEvent(
        Long subscriptionId,
        PaymentStatus status
) {}
