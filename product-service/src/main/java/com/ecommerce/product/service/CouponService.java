package com.ecommerce.product.service;

import com.ecommerce.product.model.entity.Coupon;
import com.ecommerce.product.model.request.CouponRequest;
import com.ecommerce.product.model.response.CouponResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CouponService {

    CouponResponse createCoupon(CouponRequest request);

    CouponResponse updateCoupon(Long id, CouponRequest request);

    CouponResponse getCouponById(Long id);

    List<CouponResponse> getAllCoupons();

    void deleteCoupon(Long id);

//    CouponProduct addCouponToProduct(Long couponId, String productId);

//    Optional<Coupon> findByCouponCode(String couponCode);
}
