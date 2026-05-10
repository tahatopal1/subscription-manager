package com.project.subscription.dto.event;

import com.project.subscription.entity.enums.PaymentStatus;

/**
 * Inbound event received from the Payment Service upon payment completion.
 */
public record PaymentResultEvent(
        Long subscriptionId,
        PaymentStatus status
) {}
