package com.ecommerce.order.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    @JsonIgnore
    private Cart cart;

//    @ManyToOne
//    @JoinColumn(name = "product_id")
//    private Product product;
    private String productId; // Lưu product ID thay vì quan hệ JPA
    private Double unitPrice; // Lưu giá tại thời điểm thêm vào gi
    private Integer quantity; // Thêm số lượng sản phẩm
}
