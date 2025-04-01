package com.ecommerce.order.model.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductDTO {
    private String id;
    private String name;
    private Double price;
    private String cover;
    private String brand;
    private String categoryName;
    private Integer quantity;

    @Data
    @Builder
    public static class CategoryDTO {
        private String id;
        private String name;
    }
}
