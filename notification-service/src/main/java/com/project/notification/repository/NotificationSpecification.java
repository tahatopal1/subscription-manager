package com.project.notification.repository;

import com.project.notification.dto.request.NotificationSearchRequest;
import com.project.notification.entity.Notification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specification factory for dynamic notification search.
 * Each non-null field in {@link NotificationSearchRequest} adds an AND predicate.
 */
public final class NotificationSpecification {

    private NotificationSpecification() {}

    public static Specification<Notification> from(NotificationSearchRequest criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.id() != null) {
                predicates.add(cb.equal(root.get("id"), criteria.id()));
            }
            if (criteria.userId() != null) {
                predicates.add(cb.equal(root.get("userId"), criteria.userId()));
            }
            if (criteria.eventId() != null) {
                predicates.add(cb.equal(root.get("eventId"), criteria.eventId()));
            }
            if (criteria.source() != null && !criteria.source().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("source")),
                        "%" + criteria.source().toLowerCase() + "%"));
            }
            if (criteria.channel() != null && !criteria.channel().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("channel")),
                        "%" + criteria.channel().toLowerCase() + "%"));
            }
            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }
            if (criteria.retryCount() != null) {
                predicates.add(cb.equal(root.get("retryCount"), criteria.retryCount()));
            }
            if (criteria.sentAtFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("sentAt"), criteria.sentAtFrom()));
            }
            if (criteria.sentAtTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("sentAt"), criteria.sentAtTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
