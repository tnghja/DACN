package com.ecommerce.order.controller;

import com.ecommerce.order.model.dto.PurchaseOrderDTO;
import com.ecommerce.order.model.entity.Order;
import com.ecommerce.order.model.entity.PaymentStatus;
import com.ecommerce.order.model.request.CheckoutRequest;
import com.ecommerce.order.model.response.ApiResponse;
import com.ecommerce.order.model.response.CheckoutResponse;
import com.ecommerce.order.model.response.PaymentResponse;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;

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
    //Place order
    @PostMapping("/placeOrder")
    public ResponseEntity<ApiResponse<Order>> placeOrder(
            @Valid @RequestBody PurchaseOrderDTO purchaseOrderDTO) {
        Order orderResponse = orderService.placeOrder(purchaseOrderDTO);
        ApiResponse<Order> response = new ApiResponse<>();
        response.ok(orderResponse);
        return ResponseEntity.ok(response);
    }

    // Process Payment
    @PostMapping("/processingPurchase")
    public ResponseEntity<ApiResponse<PaymentResponse.VNPayResponse>> processPurchase(
            @Valid @RequestBody Long orderId, HttpServletRequest request) {
        PaymentResponse.VNPayResponse paymentResponse = orderService.processingPurchaseOrder(orderId, request);
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
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(@PathVariable String  userId) {
        List<Order> orders = orderService.getUserOrders(userId);
        ApiResponse<List<Order>> response = new ApiResponse<>();
        response.ok(orders);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-order/{userId}/{productId}/{paymentStatus}")
    boolean existsByCustomerIdAndOrderItemsProductIdAndStatus(
            @PathVariable("userId") String userId,
            @PathVariable("productId") String productId,
            @PathVariable("paymentStatus") PaymentStatus paymentStatus
    ) {
        return orderRepository.existsByUserIdAndOrderItemsProductIdAndStatus(userId,productId,paymentStatus);
    }
}
