package com.ecommerce.product.model.dto;


import com.ecommerce.product.model.request.CheckoutRequest;
import com.ecommerce.product.model.response.CheckoutResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

@Data
public class PurchaseOrderDTO {
    @NotNull(message = "Price is mandatory")
    CheckoutResponse prices;
    @NotNull(message = "IdUser is mandatory")
    long userId;
    @NotNull(message = "checkoutReq is mandatory")
    CheckoutRequest checkoutRequest;
}