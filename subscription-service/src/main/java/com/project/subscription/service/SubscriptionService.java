package com.project.subscription.service;

import com.project.subscription.dto.request.SubscriptionSearchRequest;
import com.project.subscription.dto.request.UpdateSubscriptionRequest;
import com.project.subscription.dto.response.SubscriptionResponse;
import com.project.subscription.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SubscriptionService {

    SubscriptionResponse createSubscription(Long userId);

    List<SubscriptionResponse> getSubscriptionsByUserId(Long userId);

    SubscriptionResponse cancelSubscriptionByUserId(Long userId);

    SubscriptionResponse cancel(Long subscriptionId);

    SubscriptionResponse handlePaymentResult(Long subscriptionId, PaymentStatus paymentStatus);

    SubscriptionResponse retryPaymentByUserId(Long userId);

    SubscriptionResponse retry(Long subscriptionId);

    SubscriptionResponse reactivateByUserId(Long userId);

    SubscriptionResponse reactivate(Long subscriptionId);

    Page<SubscriptionResponse> getAllSubscriptions(SubscriptionSearchRequest request, Pageable pageable);

    SubscriptionResponse getSubscriptionById(Long subscriptionId);

    SubscriptionResponse updateSubscription(Long subscriptionId, UpdateSubscriptionRequest request);
}
