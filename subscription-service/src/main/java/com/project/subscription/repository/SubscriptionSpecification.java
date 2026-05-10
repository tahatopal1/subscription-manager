package com.project.subscription.repository;

import com.project.subscription.dto.request.SubscriptionSearchRequest;
import com.project.subscription.entity.Subscription;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionSpecification {

    public static Specification<Subscription> filterBy(SubscriptionSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request != null) {
                if (StringUtils.hasText(request.userId())) {
                    predicates.add(cb.equal(root.get("userId"), request.userId()));
                }
                
                if (request.status() != null) {
                    predicates.add(cb.equal(root.get("status"), request.status()));
                }

                if (request.startDate() != null) {
                    Instant startOfDay = request.startDate().atStartOfDay().toInstant(ZoneOffset.UTC);
                    Instant endOfDay = request.startDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
                    predicates.add(cb.between(root.get("startDate"), startOfDay, endOfDay));
                }

                if (request.endDate() != null) {
                    Instant startOfDay = request.endDate().atStartOfDay().toInstant(ZoneOffset.UTC);
                    Instant endOfDay = request.endDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
                    predicates.add(cb.between(root.get("endDate"), startOfDay, endOfDay));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
