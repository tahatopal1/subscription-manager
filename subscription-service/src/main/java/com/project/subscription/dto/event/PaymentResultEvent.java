package com.project.subscription.dto.event;

import com.project.subscription.entity.enums.PaymentStatus;

public record PaymentResultEvent(
        Long subscriptionId,
        PaymentStatus status
) {}
