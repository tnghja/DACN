package com.ecommerce.product.model.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CouponRequest {
    private String name;
    private String couponCode;
    private String discountType; // "PERCENTAGE" hoặc "FIXED"
    private Double discount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Double maxDiscountAmount;
    private Double minOrderValue;
    private Boolean isActive;
}
