package com.ecommerce.product.service.impl;


import com.ecommerce.product.config.VNPayConfig;
import com.ecommerce.product.model.response.PaymentResponse;
import com.ecommerce.product.service.PaymentService;
import com.ecommerce.product.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final VNPayConfig vnPayConfig;

    public PaymentResponse.VNPayResponse createVnPayPayment(HttpServletRequest request, Double amount, String orderInfo) {
        // long amount = Integer.parseInt(request.getParameter("amount")) * 100L;
        String bankCode = request.getParameter("bankCode");
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
        vnpParamsMap.put("vnp_Amount", String.valueOf((long) (amount * 100)));

        vnpParamsMap.put("vnp_OrderInfo", orderInfo);

         if (bankCode != null && !bankCode.isEmpty()) {
         vnpParamsMap.put("vnp_BankCode", bankCode);
         }
        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
        return PaymentResponse.VNPayResponse.builder()
                .code("ok")
                .message("success")
                .paymentUrl(paymentUrl).build();
    }
}