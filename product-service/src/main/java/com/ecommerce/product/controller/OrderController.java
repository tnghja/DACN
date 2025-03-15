package com.ecommerce.product.controller;

import com.ecommerce.product.model.dto.PurchaseOrderDTO;
import com.ecommerce.product.model.entity.Order;
import com.ecommerce.product.model.request.CheckoutRequest;
import com.ecommerce.product.model.response.ApiResponse;
import com.ecommerce.product.model.response.CheckoutResponse;
import com.ecommerce.product.model.response.PaymentResponse;
import com.ecommerce.product.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;
    // Checkout Order
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkoutOrder(@RequestBody @Valid CheckoutRequest checkoutRequest) {
        CheckoutResponse checkoutResponse = orderService.checkoutOrder(checkoutRequest);
        ApiResponse<CheckoutResponse> response = new ApiResponse<>();
        response.ok(checkoutResponse);
        return ResponseEntity.ok(response);
    }

    // Place Order
//    @PostMapping("/place")
//    public ResponseEntity<ApiResponse<Order>> placeOrder(@RequestParam @NotNull Long userId) {
//        Order order = orderService.placeOrder(userId);
//        ApiResponse<Order> response = new ApiResponse<>();
//        response.ok(order);
//        return ResponseEntity.ok(response);
//    }

    // Process Payment
    @PostMapping("/processingPurchase")
    public ResponseEntity<ApiResponse<PaymentResponse.VNPayResponse>> processPurchase(
            @Valid @RequestBody PurchaseOrderDTO purchaseOrderDTO, HttpServletRequest request) {
        PaymentResponse.VNPayResponse paymentResponse = orderService.processingPurchaseOrder(purchaseOrderDTO, request);
        ApiResponse<PaymentResponse.VNPayResponse> response = new ApiResponse<>();
        response.ok(paymentResponse);
        return ResponseEntity.ok(response);
    }

    // Complete Order
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<Void>> completeOrder(@RequestParam Map<String, String> reqParams) {
        orderService.completeOrder(reqParams);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }

    // Cancel Order
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }

    // Get Order Details
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Order>> getOrderDetails(@PathVariable Long orderId) {
        Order order = orderService.getOrderDetails(orderId);
        ApiResponse<Order> response = new ApiResponse<>();
        response.ok(order);
        return ResponseEntity.ok(response);
    }

    // Get User's Orders
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        ApiResponse<List<Order>> response = new ApiResponse<>();
        response.ok(orders);
        return ResponseEntity.ok(response);
    }
}
