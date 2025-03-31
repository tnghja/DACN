package com.ecommerce.order.controller;

import com.ecommerce.order.model.request.CouponRequest;
import com.ecommerce.order.model.response.CouponResponse;
import com.ecommerce.order.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<List<CouponResponse>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponResponse> getCouponById(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.getCouponById(id));
    }

    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@RequestBody CouponRequest coupon) {
        return ResponseEntity.ok(couponService.createCoupon(coupon));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouponResponse> updateCoupon(@PathVariable Long id, @RequestBody CouponRequest couponDetails) {
        return ResponseEntity.ok(couponService.updateCoupon(id, couponDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }
//    @PostMapping("{couponId}/add/{productId}/")
//    public ResponseEntity<CouponProduct> addCouponToProduct(@PathVariable Long couponId, @PathVariable String productId) {
//        return ResponseEntity.ok(couponService.addCouponToProduct(couponId, productId));
//   }
//    @GetMapping("/code/{couponCode}")
//    public ResponseEntity<Coupon> getCouponByCode(@PathVariable String couponCode) {
//        Optional<Coupon> coupon = couponService.findByCouponCode(couponCode);
//        return coupon.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }
}
