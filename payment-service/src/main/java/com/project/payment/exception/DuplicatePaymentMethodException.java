package com.project.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user attempts to register a payment method (gatewayToken + userId)
 * that already exists in the system.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicatePaymentMethodException extends RuntimeException {
    public DuplicatePaymentMethodException(String message) {
        super(message);
    }
}
