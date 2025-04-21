package com.ecommerce.product.model.request;

import lombok.Data;

@Data
public class OrderRequest {
    String userId;
    String orderId;
}
