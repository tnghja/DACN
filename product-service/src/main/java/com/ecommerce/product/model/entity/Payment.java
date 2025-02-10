package com.ecommerce.product.model.entity;

import com.ecommerce.product.constant.PaymentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // Example: PENDING, COMPLETED, FAILED

    private String paymentMethod; // Example: Credit Card, PayPal
    private LocalDateTime paymentDate;
}

