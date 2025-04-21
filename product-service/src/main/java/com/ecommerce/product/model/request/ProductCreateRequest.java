package com.ecommerce.product.model.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductCreateRequest {
    @NotBlank(message = "Product name is required")
    private String name;
    
    @NotBlank(message = "Brand is required")
    private String brand;
    
    private String cover;
    
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price must be greater than zero")
    private Double price;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be a non-negative value")
    private Integer quantity;
    private Double rate;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
