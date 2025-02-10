package com.ecommerce.product.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Customer extends User {
    @OneToMany(mappedBy = "customer") // Matches the field in Cart
    private List<Cart> carts;

    @OneToMany(mappedBy = "customer") // Matches the field in OrderDetail
    private List<Order> orders;

    @ManyToMany // Define this if there is a ManyToMany relation
    private List<Product> products;

    @OneToMany(mappedBy = "customer") // Matches the field in Review
    private List<Review> reviews;
}

