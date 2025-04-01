package com.ecommerce.order.controller;


import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.service.PaymentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;



@RestController
@RequestMapping("api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final OrderService orderService;

    @Value("${frontend_host:http://localhost:5173}")
    private String frontend_host;

    // @GetMapping("/vn-pay")
    // public ResponseEntity<PaymentResponse.VNPayResponse> pay(HttpServletRequest
    // request) {
    // return new
    // ResponseEntity<>(paymentService.createVnPayPayment(request),HttpStatus.OK);
    // }
    @GetMapping("/vn-pay-callback")
    public void payCallbackHandler(@RequestParam Map<String, String> reqParams, HttpServletResponse response) throws IOException {
//        LOGGER.info("VNPay Callback received: " + reqParams);
//        // Validate secure hash
//        if (!paymentService.verifyVNPaySignature(reqParams)) {
//            LOGGER.warning("Invalid VNPay Secure Hash!");
//            response.sendRedirect(frontendHost + "/payment-failed?reason=invalid_signature");
//            return;
//        }

        String responseCode = reqParams.get("vnp_ResponseCode");

        if (Objects.equals(responseCode, "00")) {
            // Payment successful, complete the order
            orderService.completeOrder(reqParams);
            response.sendRedirect(frontend_host + "/payment-success");
        } else {
            response.sendRedirect(frontend_host + "/payment-failed?reason=" + responseCode);
        }
    }
}