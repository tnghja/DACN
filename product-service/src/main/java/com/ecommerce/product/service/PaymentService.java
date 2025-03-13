package com.ecommerce.product.service;

import com.ecommerce.product.model.response.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    PaymentResponse.VNPayResponse createVnPayPayment(HttpServletRequest request, Double amount, String orderInfo);
}