package com.ecommerce.order.model.mapper;




import com.ecommerce.order.model.entity.Coupon;
import com.ecommerce.order.model.request.CouponRequest;
import com.ecommerce.order.model.response.CouponResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.context.annotation.Bean;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CouponMapper {

    Coupon toEntity(CouponRequest request);

    CouponResponse toResponse(Coupon coupon);

    List<CouponResponse> toResponseList(List<Coupon> coupons);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCouponFromRequest(CouponRequest request, @MappingTarget Coupon coupon);
}
