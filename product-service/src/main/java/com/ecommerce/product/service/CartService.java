package com.ecommerce.product.service;

import com.ecommerce.product.model.entity.Cart;
import com.ecommerce.product.model.entity.Order;
import com.ecommerce.product.model.response.CartResponse;

public interface CartService {
    Cart getCartById(Long userId);

    Cart createCart(Long userId);

    void addProductToCart(Long userId, String productId, Integer quantity);

    CartResponse getCartByUserId(Long userId);

    void deleteProductFromCart(Long userId, String productId);

    void deleteAllProductFromCart(Long userId);

    void deleteCart(Long userId);

//    Order copyCartToOrder(Long userId);

    void updateProductQuantityInCart(Long userId, String productId, Integer quantity);
}
