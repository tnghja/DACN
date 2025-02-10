package com.ecommerce.product.controller;

import com.ecommerce.product.model.entity.Cart;
import com.ecommerce.product.model.entity.Product;
import com.ecommerce.product.model.response.ApiResponse;
import com.ecommerce.product.model.response.CartResponse;
import com.ecommerce.product.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@PathVariable Long userId) {
        CartResponse cart = cartService.getCartByUserId(userId);
        ApiResponse<CartResponse> response = new ApiResponse<>();
        response.ok(cart);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create/{studentId}")
    public ResponseEntity<ApiResponse<Cart>> createCart(@PathVariable Long studentId) {
        Cart cart = cartService.createCart(studentId);
        ApiResponse<Cart> response = new ApiResponse<>();
        response.ok(cart);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/add/{productId}/{quantity}")
    public ResponseEntity<Void> addProduct(@PathVariable Long userId,
                                           @PathVariable Long productId,
                                           @PathVariable Long quantity) {
        cartService.addProductToCart(userId, productId, quantity);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{userId}/remove/{courseId}")
    public ResponseEntity<ApiResponse<Void>> removeProduct(@PathVariable Long userId, @PathVariable Long courseId) {
        cartService.deleteProductFromCart(userId, courseId);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable Long userId) {
        cartService.deleteAllCourseFromCart(userId);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteCart(@PathVariable Long userId) {
        cartService.deleteCart(userId);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }
}
