package com.project.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicatePaymentMethodException extends RuntimeException {
    public DuplicatePaymentMethodException(String message) {
        super(message);
    }
}
