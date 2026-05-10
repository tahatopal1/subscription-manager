package com.project.subscription.dto.request;

import com.project.subscription.entity.enums.SubscriptionStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record SubscriptionSearchRequest(
        String userId,
        SubscriptionStatus status,
        
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,
        
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate
) {
}
