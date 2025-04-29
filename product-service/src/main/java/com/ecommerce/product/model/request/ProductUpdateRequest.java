package com.ecommerce.product.model.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductUpdateRequest {
    private String name;
    private String brand;
    private String cover;
    private String description;
    @DecimalMin(value = "0.0", message = "Price must be greater than zero")
    private Double price;
    @Min(value = 0, message = "Quantity must be a non-negative value")
    private Integer quantity;
    private Long categoryId;
}
