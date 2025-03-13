package com.ecommerce.product.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

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
    private Date orderDate;
    private Double totalPrice;
    private PaymentStatus status;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;
    @OneToOne
    @JoinColumn(name = "user_payment_id")
    private UserPayment userPayment;
}
