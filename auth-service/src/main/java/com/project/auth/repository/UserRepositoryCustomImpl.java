package com.project.auth.repository;

import com.project.auth.dto.request.admin.UserSearchRequest;
import com.project.auth.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<User> searchUsers(UserSearchRequest request, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // Count Query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);
        countQuery.select(cb.count(countRoot));
        
        List<Predicate> predicates = buildPredicates(request, cb, countRoot);
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        
        List<Predicate> resultPredicates = buildPredicates(request, cb, root);
        if (!resultPredicates.isEmpty()) {
            query.where(cb.and(resultPredicates.toArray(new Predicate[0])));
        }
        
        if (pageable.getSort().isSorted()) {
            List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(root.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(root.get(order.getProperty())));
                }
            });
            query.orderBy(orders);
        }

        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<User> content = typedQuery.getResultList();

        return new PageImpl<>(content, pageable, total);
    }

    private List<Predicate> buildPredicates(UserSearchRequest request, CriteriaBuilder cb, Root<User> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (request != null) {
            if (StringUtils.hasText(request.email())) {
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + request.email().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(request.name())) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + request.name().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(request.surname())) {
                predicates.add(cb.like(cb.lower(root.get("surname")), "%" + request.surname().toLowerCase() + "%"));
            }
            if (StringUtils.hasText(request.role())) {
                predicates.add(cb.isMember(request.role(), root.get("roles")));
            }
        }

        return predicates;
    }
}
