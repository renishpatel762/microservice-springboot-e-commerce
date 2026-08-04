package com.ecommerce.order.repository;

import com.ecommerce.order.dto.OrderSearchCriteria;
import com.ecommerce.order.entity.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<Order> buildSpecification(final OrderSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }

            if (criteria.customerEmail() != null && !criteria.customerEmail().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("customerEmail")), criteria.customerEmail().trim().toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
