package com.ecommerce.product.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private Long parentId;
    private List<CategoryDTO> subCategories;
}
