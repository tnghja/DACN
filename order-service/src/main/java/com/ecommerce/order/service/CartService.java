package com.ecommerce.order.service;


import com.ecommerce.order.model.entity.Cart;
import com.ecommerce.order.model.response.CartResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CartService {

    Cart getCartById(String customerId);

    Cart createCart(String customerId);

    void addProductToCart(String userId, String productId, Integer quantity);


    CartResponse getCartByUserId(String userId);

    void deleteProductFromCart(String userId, String productId);


    @Transactional
    void deleteAllProductFromCart(String userId);

//    Order copyCartToOrder(Long userId);
public void updateProductQuantityInCart(String userId, String productId, Integer quantity);
    void deleteCart(String userId);

    void deleteListProductFromCart(String userId, List<String> productIds);
}
