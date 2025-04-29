package com.ecommerce.order.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Billing details
    private String firstName;
    private String lastName;
    private String country;
    private String streetAddress;
    private String city;

    private Double subtotal;
    private String shipping;
    private Double vat;
    private Double total;

    // Payment method
    private String paymentMethod;
}
