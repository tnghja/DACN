package com.ecommerce.search_service.model.mapper;

import com.ecommerce.search_service.model.entity.Category;
import com.ecommerce.search_service.model.entity.Product;
import com.ecommerce.search_service.model.entity.ProductDocument;
import com.ecommerce.search_service.model.response.ProductResponse;
import com.ecommerce.search_service.repository.CategoryRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
@Mapper(componentModel = "spring")
public abstract class ProductMapper {
    @Autowired
    protected CategoryRepository categoryRepository;


    @Mapping(source = "category.name", target = "categoryName")
    public abstract ProductResponse toResponse(Product product);

    @Mapping(target = "categoryName", expression = "java(resolveCategoryName(productDocument.getCategoryId()))")
    public abstract ProductResponse toResponse(ProductDocument productDocument);

    @Named("resolveCategoryName")
    protected String resolveCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "Unknown1";
        }
        return categoryRepository.findById(categoryId)
                .map(Category::getName)
                .orElse("Unknown2");
    }
}