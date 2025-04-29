package com.ecommerce.product.repository;

import com.ecommerce.product.model.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByProductId(String id);
    Page<Rating> findByProductId(String productId, Pageable pageable);
    Optional<Rating> findByCustomerIdAndProductId(String userId, String productId);
}
