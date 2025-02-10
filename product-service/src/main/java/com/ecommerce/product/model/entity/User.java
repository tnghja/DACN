package com.ecommerce.product.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "users")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private LocalDateTime createAt;
    private LocalDateTime deleteAt;

    @OneToMany(mappedBy = "user")
    private List<UserAddress> addresses;

    @OneToMany(mappedBy = "user")
    private List<UserPayment> payments;

    @OneToOne(mappedBy = "user")
    private PasswordReset passwordReset;

    @OneToMany(mappedBy = "user")
    private List<SearchHistory> searchHistories;
}
