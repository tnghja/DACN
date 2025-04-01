package com.ecommerce.order.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class CartResponse {
    private Long cartId;
    private String userId;
    private Double total;
    private List<CartItemResponse> products; // Now includes quantity per product

}
