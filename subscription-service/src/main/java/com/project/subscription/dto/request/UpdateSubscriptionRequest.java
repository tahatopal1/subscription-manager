package com.project.subscription.dto.request;

import com.project.subscription.entity.enums.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Admin-only request body for patching the mutable lifecycle fields of a subscription.
 *
 * <p>All three fields are optional — only non-null values are applied (partial update semantics).
 * At least one field must be present; the service layer enforces this.
 *
 * <p>Date validation note: startDate / endDate are intentionally not annotated with {@code @Future}
 * because admins may legitimately backdate a subscription (e.g., for manual corrections).
 */
public record UpdateSubscriptionRequest(

        /**
         * New status to set. When provided, the transition is applied without writing an
         * outbox event — use the retry/cancel endpoints for event-driven status changes.
         */
        SubscriptionStatus status,

        /** New billing start date. Null means "leave unchanged". */
        Instant startDate,

        /** New billing end date. Null means "leave unchanged". */
        Instant endDate
) {
    /** Returns true when at least one field carries a value to apply. */
    public boolean hasAnyField() {
        return status != null || startDate != null || endDate != null;
    }
}
