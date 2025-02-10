package com.ecommerce.product.service;

import com.ecommerce.product.model.entity.Cart;
import com.ecommerce.product.model.entity.Product;
import com.ecommerce.product.model.response.CartResponse;

public interface CartService {
    Cart getCartById(Long userId);

    Cart createCart(Long studentId);

    Product addProductToCart(Long userId, Long productId, Long quantity);

    CartResponse getCartByUserId(Long studentId);

    void deleteProductFromCart(Long studentId, Long courseId);

    void deleteAllCourseFromCart(Long userId);

    void deleteCart(Long userId);
}
