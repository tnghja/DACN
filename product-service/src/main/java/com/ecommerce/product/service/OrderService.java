//package com.ecommerce.product.service;
//
//import java.util.List;
//import java.util.Map;
//
//
//import com.ecommerce.product.model.dto.PurchaseOrderDTO;
//import com.ecommerce.product.model.entity.Order;
//import com.ecommerce.product.model.request.CheckoutRequest;
//import com.ecommerce.product.model.response.CheckoutResponse;
//import com.ecommerce.product.model.response.PaymentResponse;
//import jakarta.servlet.http.HttpServletRequest;
//
//public interface OrderService {
//
//
//    CheckoutResponse checkoutOrder(CheckoutRequest request);
//
//    Order placeOrder(Long userId, Long couponId);
//
//    PaymentResponse.VNPayResponse processingPurchaseOrder(PurchaseOrderDTO purchaseOrderDTO,
//                                                          HttpServletRequest request);
//
//    void completeOrder(Map<String, String> reqParams);
//
//    void cancelOrder(Long orderId);
//
//    Order getOrderDetails(Long orderId);
//
//    List<Order> getUserOrders(Long userId);
//}