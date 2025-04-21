package com.ecommerce.order.model.dto;


import com.ecommerce.order.model.request.CheckoutRequest;
import com.ecommerce.order.model.request.OrderDetailRequest;
import com.ecommerce.order.model.response.CheckoutResponse;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class PurchaseOrderDTO {
    @NotNull(message = "Price is mandatory")
    CheckoutResponse prices;
    @NotNull(message = "IdUser is mandatory")
    String userId;
    @NotNull(message = "checkoutReq is mandatory")
    CheckoutRequest checkoutRequest;

    OrderDetailRequest orderDetail;
}