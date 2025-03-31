package com.ecommerce.order.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryRequest {
    private String productId;
    private Integer quantity;
}