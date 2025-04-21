package com.ecommerce.product.model.request;

import lombok.Data;

@Data
public class InventoryRequest {
    private String productId; // ID của sản phẩm
    private Integer quantity; // Số lượng cần kiểm tra
}