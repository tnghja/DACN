package com.ecommerce.order.repository;


import com.ecommerce.order.model.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCouponCode(String couponCode);

    boolean existsByCouponCode(String couponCode);
}
