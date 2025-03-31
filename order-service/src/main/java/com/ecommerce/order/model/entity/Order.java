package com.ecommerce.order.model.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Setter
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double itemTotal;
    private Double shipFee;
    private Double totalDiscount;
    private String shipCode;
    private LocalDateTime orderDate;
    private Double totalPrice;
    private PaymentStatus status;

    @OneToOne
    private Coupon coupon;
//    @ManyToOne
//    @JoinColumn(name = "customer_id")
//    private User customer;

    private String userId;
    @OneToOne
    @JoinColumn(name = "user_payment_id")
    private UserPayment userPayment;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;
}
