package com.ecommerce.order.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResponse {
    private Double totalPrice;
    private Double discountPrice;
    private Double finalPrice;
}
