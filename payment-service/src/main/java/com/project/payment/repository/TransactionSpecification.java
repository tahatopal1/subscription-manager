package com.project.payment.repository;

import com.project.payment.dto.request.TransactionSearchCriteria;
import com.project.payment.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    public static Specification<Transaction> filterBy(TransactionSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria != null) {
                if (StringUtils.hasText(criteria.userId())) {
                    predicates.add(cb.equal(root.get("userId"), criteria.userId()));
                }
                if (criteria.status() != null) {
                    predicates.add(cb.equal(root.get("status"), criteria.status()));
                }
                if (criteria.fromDate() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.fromDate().atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));
                }
                if (criteria.toDate() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.toDate().plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC)));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
