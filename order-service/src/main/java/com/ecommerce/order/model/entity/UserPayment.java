package com.ecommerce.order.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
public class UserPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    private String paymentType;
    private String provider;
    private String accountNo;
    private LocalDate expiry;

    private String userId;
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;

    @OneToOne (mappedBy = "userPayment")
    private Order order;
}
