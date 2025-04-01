package com.ecommerce.order.model.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CouponResponse {
    private Long id;
    private String name;
    private String couponCode;
    private String discountType;
    private Double discount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usedCount;
    private Double maxDiscountAmount;
    private Double minOrderValue;
    private Boolean isActive;
}
