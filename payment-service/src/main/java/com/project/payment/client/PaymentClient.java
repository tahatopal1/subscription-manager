package com.project.payment.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.web.client.HttpServerErrorException;

@Slf4j
@Component
public class PaymentClient {

    @Retryable(
        retryFor = { HttpServerErrorException.class},
        backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public String charge(String gatewayToken, BigDecimal amount) {
        log.info("Client: Charging token={} amount={}", gatewayToken, amount);
        // Simulate successful charge
        return "PY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
