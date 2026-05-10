package com.project.notification.exception;

/**
 * Thrown by a NotificationProvider when the upstream delivery channel
 * (e.g., Twilio, AWS SES) is temporarily unavailable.
 * Triggers FAILED status and makes the notification eligible for retry.
 */
public class ProviderUnavailableException extends RuntimeException {

    public ProviderUnavailableException(String message) {
        super(message);
    }

    public ProviderUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
