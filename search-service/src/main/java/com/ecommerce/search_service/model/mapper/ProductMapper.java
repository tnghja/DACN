package com.ecommerce.search_service.model.mapper;

import com.ecommerce.search_service.model.entity.Product;
import com.ecommerce.search_service.model.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(source = "category.name", target = "categoryName") // Map category name
    ProductResponse toResponse(Product product);
}