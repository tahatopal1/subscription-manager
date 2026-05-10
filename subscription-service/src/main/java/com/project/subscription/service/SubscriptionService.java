package com.project.subscription.service;

import com.project.subscription.dto.request.SubscriptionSearchRequest;
import com.project.subscription.dto.request.UpdateSubscriptionRequest;
import com.project.subscription.dto.response.SubscriptionResponse;
import com.project.subscription.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Contract for subscription lifecycle operations.
 *
 * User-facing methods enforce ownership via userId (from the X-User-Id gateway header).
 * Search and lookup methods are open to both users and admins; the controller layer
 * applies the appropriate authorization and parameter scoping.
 */
public interface SubscriptionService {

    /**
     * Creates a new PENDING subscription for the authenticated user.
     * Atomically writes a SubscriptionInitiatedEvent to the outbox.
     * Returns 202 Accepted semantics — activation is async via payment callback.
     *
     * @param userId  resolved from X-User-Id header
     * @return the newly created subscription (status = PENDING)
     */
    SubscriptionResponse createSubscription(Long userId);

    /**
     * Lists all subscriptions owned by the authenticated user.
     *
     * @param userId resolved from X-User-Id header
     * @return list of the user's subscriptions
     */
    List<SubscriptionResponse> getSubscriptionsByUserId(Long userId);

    /**
     * Schedules the caller's ACTIVE subscription for cancellation at period end.
     * Finds the single ACTIVE subscription owned by the user — no client-supplied ID needed.
     *
     * @param userId resolved from JWT — enforces ownership
     * @return the updated subscription (status still ACTIVE, cancelAtPeriodEnd = true)
     */
    SubscriptionResponse cancelSubscriptionByUserId(Long userId);

    /**
     * Admin-only: schedules a specific subscription for cancellation at period end.
     *
     * @param subscriptionId the exact subscription to cancel
     * @return the updated subscription
     */
    SubscriptionResponse cancel(Long subscriptionId);

    /**
     * Processes an inbound asynchronous payment result. Idempotent — already-resolved states are
     * silently ignored.
     *
     * @param subscriptionId target subscription
     * @param paymentStatus  SUCCESS or FAILED
     * @return
     */
    SubscriptionResponse handlePaymentResult(Long subscriptionId, PaymentStatus paymentStatus);

    /**
     * Retries the payment for the caller's PENDING subscription.
     * Finds the first PENDING subscription owned by the user.
     *
     * @param userId resolved from JWT — enforces ownership; no client-supplied ID
     * @return the subscription (still PENDING until payment confirms)
     */
    SubscriptionResponse retryPaymentByUserId(Long userId);

    /**
     * Admin-only: retries payment for a specific PENDING subscription.
     *
     * @param subscriptionId the exact subscription to retry
     * @return the subscription (still PENDING until payment confirms)
     */
    SubscriptionResponse retry(Long subscriptionId);

    /**
     * Reactivates the caller's ACTIVE subscription that was scheduled for cancellation.
     * Finds the ACTIVE subscription with cancelAtPeriodEnd=true — no client-supplied ID needed.
     *
     * @param userId resolved from JWT — enforces ownership
     * @return the updated subscription (status = ACTIVE, cancelAtPeriodEnd = false)
     */
    SubscriptionResponse reactivateByUserId(Long userId);

    /**
     * Admin-only: reactivates a specific subscription that was scheduled for cancellation.
     *
     * @param subscriptionId the exact subscription to reactivate
     * @return the updated subscription (cancelAtPeriodEnd = false)
     */
    SubscriptionResponse reactivate(Long subscriptionId);

    /**
     * Paginated search across all subscriptions with optional filters.
     */
    Page<SubscriptionResponse> getAllSubscriptions(SubscriptionSearchRequest request, Pageable pageable);

    /**
     * Retrieves a single subscription by its ID.
     */
    SubscriptionResponse getSubscriptionById(Long subscriptionId);

    /**
     * Admin-only partial update for a subscription's mutable lifecycle fields.
     * Only non-null fields in the request are applied (patch semantics).
     * Does not emit outbox events — use the domain-specific endpoints for event-driven transitions.
     *
     * @param subscriptionId target subscription
     * @param request        fields to update; at least one must be non-null
     * @return the updated subscription
     */
    SubscriptionResponse updateSubscription(Long subscriptionId, UpdateSubscriptionRequest request);
}
