package com.ecommerce.product.model.mapper;


import com.ecommerce.product.model.entity.Coupon;
import com.ecommerce.product.model.request.CouponRequest;
import com.ecommerce.product.model.response.CouponResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CouponMapper {

    Coupon toEntity(CouponRequest request);

    CouponResponse toResponse(Coupon coupon);

    List<CouponResponse> toResponseList(List<Coupon> coupons);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCouponFromRequest(CouponRequest request, @MappingTarget Coupon coupon);
}
