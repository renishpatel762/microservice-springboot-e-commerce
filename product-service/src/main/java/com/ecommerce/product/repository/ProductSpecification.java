package com.ecommerce.product.repository;

import com.ecommerce.product.dto.ProductSearchCriteria;
import com.ecommerce.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic JPA Specification builder for Product dynamic search queries.
 */
public final class ProductSpecification {

    private ProductSpecification() {
        // Utility class private constructor
    }

    /**
     * Builds dynamic JPA Specification predicate based on provided search criteria.
     */
    public static Specification<Product> buildSpecification(final ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.category() != null && !criteria.category().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), criteria.category().trim().toLowerCase()));
            }

            if (criteria.name() != null && !criteria.name().isBlank()) {
                String pattern = "%" + criteria.name().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }

            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.minPrice()));
            }

            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.maxPrice()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
