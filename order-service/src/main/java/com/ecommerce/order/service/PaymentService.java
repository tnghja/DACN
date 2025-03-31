package com.ecommerce.order.service;


import com.ecommerce.order.model.response.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    PaymentResponse.VNPayResponse createVnPayPayment(HttpServletRequest request, Double amount, String orderInfo);
}