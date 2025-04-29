package com.ecommerce.order.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    private Double total;

//    @JsonIgnore
//    @ManyToOne
//    @JoinColumn(name = "customer_id")
//    private User customer;
    private String customerId;
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<CartItem> cartItems;
}
