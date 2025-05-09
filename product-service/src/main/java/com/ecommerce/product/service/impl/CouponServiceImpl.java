//
//package com.ecommerce.product.service.impl;
//
//import com.ecommerce.product.model.entity.Coupon;
//
//import com.ecommerce.product.model.entity.Product;
//import com.ecommerce.product.model.mapper.CouponMapper;
//import com.ecommerce.product.model.request.CouponRequest;
//import com.ecommerce.product.model.response.CouponResponse;
//
//import com.ecommerce.product.repository.CouponRepository;
//import com.ecommerce.product.repository.ProductRepository;
//import com.ecommerce.product.service.CouponService;
//import jakarta.persistence.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class CouponServiceImpl implements CouponService {
//
//    private final CouponRepository couponRepository;
//    private final CouponMapper couponMapper;
//
//    private final ProductRepository productRepository;
//
//    @Override
//    public CouponResponse createCoupon(CouponRequest request) {
//        if (couponRepository.existsByCouponCode(request.getCouponCode())) {
//            throw new RuntimeException("Coupon code already exists");
//        }
//
//        Coupon coupon = couponMapper.toEntity(request);
//        coupon.setCouponCode(request.getCouponCode().toUpperCase().trim());
//        coupon.setDiscountType(request.getDiscountType().toUpperCase().trim());
//        coupon.setUsedCount(0); // Khi tạo coupon mới, số lần sử dụng mặc định là 0
//        coupon.setIsActive(request.getIsActive() != null ? request.getIsActive() : true); // Mặc định kích hoạt nếu không có giá trị
//
//        coupon = couponRepository.save(coupon);
//        return couponMapper.toResponse(coupon);
//    }
//    @Override
//    public CouponResponse updateCoupon(Long id, CouponRequest request) {
//        Coupon coupon = couponRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Coupon not found"));
//
//        if (!coupon.getCouponCode().equalsIgnoreCase(request.getCouponCode()) &&
//                couponRepository.existsByCouponCode(request.getCouponCode())) {
//            throw new RuntimeException("Coupon code already exists");
//        }
//
//        couponMapper.updateCouponFromRequest(request, coupon);
//        coupon.setCouponCode(request.getCouponCode().toUpperCase().trim());
//        coupon.setDiscountType(request.getDiscountType().toUpperCase().trim());
//
//        coupon = couponRepository.save(coupon);
//        return couponMapper.toResponse(coupon);
//    }
//    @Override
//    public CouponResponse getCouponById(Long id) {
//        Coupon coupon = couponRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Coupon not found"));
//        return couponMapper.toResponse(coupon);
//    }
//    @Override
//    public List<CouponResponse> getAllCoupons() {
//        return couponMapper.toResponseList(couponRepository.findAll());
//    }
//    @Override
//    public void deleteCoupon(Long id) {
//        if (!couponRepository.existsById(id)) {
//            throw new RuntimeException("Coupon not found");
//        }
//        couponRepository.deleteById(id);
//    }
//
//}
