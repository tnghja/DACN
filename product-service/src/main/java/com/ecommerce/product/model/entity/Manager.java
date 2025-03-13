package com.ecommerce.product.model.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@DiscriminatorValue(value = UserRole.Role.MANAGER)
public class Manager extends User{
    @OneToMany
    private List<Coupon> coupons;
}
