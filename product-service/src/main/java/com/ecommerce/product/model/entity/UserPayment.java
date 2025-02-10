package com.ecommerce.product.model.entity;

import com.ecommerce.product.model.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne (mappedBy = "userPayment")
    private Order order;
}
