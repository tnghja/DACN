package com.ecommerce.product.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;

@Data
public class CheckoutRequest {
    @NotNull(message = "idCart is mandatory")
    Long CartId;
//    @NotNull(message = "idCartItems is mandatory")
//    String[] productIds;
    Long DiscountId;
}
