package com.project.payment.dto.response;

import com.project.payment.entity.PaymentMethod;

public record PaymentMethodResponse(
    Long id,
    String lastFour,
    String brand,
    boolean isDefault
) {
    public static PaymentMethodResponse from(PaymentMethod pm) {
        return new PaymentMethodResponse(
            pm.getId(),
            "**** **** **** " + pm.getLastFour(),
            pm.getBrand(),
            pm.isDefault()
        );
    }
}
