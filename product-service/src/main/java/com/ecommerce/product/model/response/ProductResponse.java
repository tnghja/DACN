package com.ecommerce.product.model.response;

import com.ecommerce.product.model.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ProductResponse {
    private Long productId;
    private String name;
    private Double price;
    private Long quantity; // Add quantity here

    // Convert from CartItem instead of Product
    public static ProductResponse fromEntity(CartItem cartItem) {
        return new ProductResponse(
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getPrice(),
                cartItem.getQuantity() // Get quantity from CartItem
        );
    }
}
