package com.project.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddPaymentMethodRequest(
    @NotBlank String gatewayToken,
    @NotBlank @Size(min = 4, max = 4) String lastFour,
    @NotBlank String brand,
    boolean isDefault
) {}