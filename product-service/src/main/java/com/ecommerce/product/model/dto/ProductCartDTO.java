package com.ecommerce.product.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCartDTO {
    private String id;
    private String name;
    private Double price;
    private String cover;
    private String brand;
    private String categoryName;
    @Data
    @Builder
    public static class CategoryDTO {
        private String id;
        private String name;
    }
}
