package com.ecommerce.search_service.repository;

import com.ecommerce.search_service.model.entity.Product;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {
    public static Specification<Product> filterBy(String name, String categoryName, Double minPrice, Double maxPrice, Double minRate, Double maxRate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }

            if (categoryName != null && !categoryName.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("category").get("name")), categoryName.toLowerCase()));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            if (minRate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rate"), minRate));
            }

            if (maxRate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rate"), maxRate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
