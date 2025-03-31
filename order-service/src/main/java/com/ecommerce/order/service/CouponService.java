package com.ecommerce.order.service;


import com.ecommerce.order.model.request.CouponRequest;
import com.ecommerce.order.model.response.CouponResponse;

import java.util.List;

public interface CouponService {

    CouponResponse createCoupon(CouponRequest request);

    CouponResponse updateCoupon(Long id, CouponRequest request);

    CouponResponse getCouponById(Long id);

    List<CouponResponse> getAllCoupons();

    void deleteCoupon(Long id);

//    CouponProduct addCouponToProduct(Long couponId, String productId);

//    Optional<Coupon> findByCouponCode(String couponCode);
}
