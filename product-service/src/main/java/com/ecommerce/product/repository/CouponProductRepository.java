//package com.ecommerce.product.repository;
//
//import com.ecommerce.product.model.entity.CouponProduct;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
//
//public interface CouponProductRepository extends JpaRepository<CouponProduct, Long> {
//    List<CouponProduct> findByCouponId(Long couponId);
//    List<CouponProduct> findByProductId(String productId);
//    CouponProduct findByCouponIdAndProductId(Long couponId, String productId);
//}
