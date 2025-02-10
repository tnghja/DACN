package com.ecommerce.product.model.entity;

import com.ecommerce.product.model.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    private String addressLine1;
    private String addressLine2;
    private String town;
    private String district;
    private String province;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
