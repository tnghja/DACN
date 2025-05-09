package com.ecommerce.product.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDTO {
    private String id;
    private String name;
    private String brand;
    private String cover;
    private String description;
    private Double price;
    private Integer quantity;
    private Double rate;
    private LocalDateTime deleteAt;
    private Long categoryId;
    private String categoryName;
    private List<String> imageUrls;
}
