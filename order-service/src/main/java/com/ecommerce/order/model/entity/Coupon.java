package com.ecommerce.order.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String couponCode;
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;// "PERCENTAGE" hoặc "FIXED"
    private Double discount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private Integer usedCount = 0;
    private Double maxDiscountAmount;
    private Double minOrderValue;
    private Boolean isActive;

    @Version
    private Long version;
//    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<CouponProduct> couponProducts;
}
