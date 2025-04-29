package com.ecommerce.product.repository.httpClient;

import com.ecommerce.product.model.entity.PaymentStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
@Repository
@FeignClient(name = "order-service",
        url = "${app.services.order}"
//        configuration = ProductErrorDecoder.class
)
public interface OrderClient {

    @GetMapping("/check-order/{userId}/{productId}/{paymentStatus}")
    boolean existsByCustomerIdAndOrderItemsProductIdAndStatus(
            @PathVariable("userId") String userId,
            @PathVariable("productId") String productId,
            @PathVariable("paymentStatus") PaymentStatus paymentStatus
    );
}



