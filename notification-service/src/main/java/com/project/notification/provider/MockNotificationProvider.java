package com.project.notification.provider;

import com.project.notification.exception.ProviderUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class MockNotificationProvider implements NotificationProvider {

    private static final double FAILURE_RATE = 0.05;

    @Override
    public void send(String userId, String message) {
        simulateNetworkLatency();
        simulateRandomFailure(userId);

        log.info("📨 MOCK NOTIFICATION SENT TO [userId={}]: {}", userId, message);
    }

    // ---- Private helpers ---------------------------------------------------

    private void simulateNetworkLatency() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("MockNotificationProvider: sleep interrupted");
        }
    }

    private void simulateRandomFailure(String userId) {
        if (ThreadLocalRandom.current().nextDouble() < FAILURE_RATE) {
            log.warn("⚠️ MOCK PROVIDER UNAVAILABLE — simulated transient failure [userId={}]", userId);
            throw new ProviderUnavailableException(
                    "Mock provider transient failure for userId=" + userId);
        }
    }
}
