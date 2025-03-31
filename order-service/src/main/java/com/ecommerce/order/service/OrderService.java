package com.ecommerce.order.service;


import com.ecommerce.order.model.dto.PurchaseOrderDTO;
import com.ecommerce.order.model.entity.Order;
import com.ecommerce.order.model.request.CheckoutRequest;
import com.ecommerce.order.model.response.CheckoutResponse;
import com.ecommerce.order.model.response.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface OrderService {


    CheckoutResponse checkoutOrder(CheckoutRequest request);


    void completeOrder(Map<String, String> reqParams);

    void cancelOrder(Long orderId);

    Order getOrderDetails(Long orderId);

    List<Order> getUserOrders(String userId);


    @Transactional
    Order placeOrder(String userId, Long couponId);

    PaymentResponse.VNPayResponse processingPurchaseOrder(PurchaseOrderDTO purchaseOrderDTO, HttpServletRequest request);
}