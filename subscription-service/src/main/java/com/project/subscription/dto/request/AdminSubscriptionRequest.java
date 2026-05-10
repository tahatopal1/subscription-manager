package com.project.subscription.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Admin-only request body for creating or modifying a subscription on behalf of a user.
 *
 * Elevated fields (e.g., overrideEndDate) are intentionally absent from UserSubscriptionRequest
 * to prevent privilege escalation via the user-facing API.
 */
public record AdminSubscriptionRequest(

        /**
         * Optional admin override for the subscription end date.
         * If null, normal 30-day billing cycle applies.
         * Must be in the future if provided.
         */
        @Future(message = "overrideEndDate must be a future date")
        Instant overrideEndDate
) {}
