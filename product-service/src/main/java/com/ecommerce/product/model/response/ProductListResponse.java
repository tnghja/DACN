package com.ecommerce.product.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductListResponse {
    private Long id;
    private String name;
    private String brand;
    private String cover;
    private Double price;
}
