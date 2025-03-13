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
@RequestMapping("api/carts")
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

//    @PostMapping("/create/{userId}")
//    public ResponseEntity<ApiResponse<Cart>> createCart(@PathVariable Long userId) {
//        Cart cart = cartService.createCart(userId);
//        ApiResponse<Cart> response = new ApiResponse<>();
//        response.ok(cart);
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/{userId}/add/{productId}/{quantity}")
    public ResponseEntity<ApiResponse<Void>> addProduct(@PathVariable Long userId,
                                           @PathVariable String productId,
                                           @PathVariable Integer quantity) {
        cartService.addProductToCart(userId, productId, quantity);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }


//    @DeleteMapping("/{userId}/remove/{courseId}")
//    public ResponseEntity<ApiResponse<Void>> removeProduct(@PathVariable Long userId, @PathVariable String productId) {
//        cartService.deleteProductFromCart(userId, productId);
//        ApiResponse<Void> response = new ApiResponse<>();
//        response.ok();
//        return ResponseEntity.ok(response);
//    }
//
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable Long userId) {
        cartService.deleteAllProductFromCart(userId);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/update/{productId}")
    public ResponseEntity<ApiResponse<Void>> updateProductQuantity(
            @PathVariable Long userId,
            @PathVariable String productId,
            @RequestParam Integer quantity) {

        cartService.updateProductQuantityInCart(userId, productId, quantity);
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
