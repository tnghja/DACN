package com.ecommerce.product.repository.httpClient;

import com.ecommerce.product.model.entity.PaymentStatus;
import com.ecommerce.product.model.request.ProcessPurchaseRequest;
import com.ecommerce.product.model.response.ApiResponse;
import com.ecommerce.product.model.response.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Repository
@FeignClient(name = "order-service",
        url = "${app.services.order}"
//        configuration = ProductErrorDecoder.class
)
public interface OrderClient {

    @GetMapping("/api/orders/check-order/{userId}/{productId}/{paymentStatus}")
    ResponseEntity<Boolean> existsByCustomerIdAndOrderItemsProductIdAndStatus(
            @PathVariable("userId") String userId,
            @PathVariable("productId") String productId,
            @PathVariable("paymentStatus") PaymentStatus paymentStatus
    );
    
    @PostMapping("/api/orders/processingPurchase")
    ApiResponse<PaymentResponse.VNPayResponse> processPurchase(
            @Valid @RequestBody ProcessPurchaseRequest request
    );
}



