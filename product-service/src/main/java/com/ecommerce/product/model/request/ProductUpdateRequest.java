package com.ecommerce.product.model.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductUpdateRequest {
    private String name;
    private String brand;
    private String cover;
    private String description;
    private Double price;
    private Integer quantity;
    private Double rate;
    private Long categoryId;
}
