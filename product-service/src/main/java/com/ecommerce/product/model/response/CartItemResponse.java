package com.ecommerce.product.model.response;

import com.ecommerce.product.model.entity.CartItem;
import com.ecommerce.product.model.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class CartItemResponse {
    private String id;
    private String name;
    private String brand;
    private String cover;
    private Double price;
    private Boolean inStock;  // ✅ Chỉ gửi thông tin có hàng hay không
    private Boolean isAvailable; // ✅ Kiểm tra xem sản phẩm còn bán không

    public static CartItemResponse fromEntity(CartItem cartItem) {
        return new CartItemResponse(
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getBrand(),
                cartItem.getProduct().getCover(),
                cartItem.getProduct().getPrice(),
                cartItem.getProduct().getQuantity() > 0,  // ✅ Kiểm tra còn hàng
                cartItem.getProduct().getDeleteAt() == null  // ✅ Kiểm tra sản phẩm còn bán không
                // ✅ Thêm số lượng sản phẩm trong giỏ hàng
        );
    }
}
