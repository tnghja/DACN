package com.ecommerce.product.model.request;

import lombok.Data;
import java.util.List;

@Data
public class OrderInventoryRequest {
    private Long orderId;
    private List<InventoryRequest> items;
}