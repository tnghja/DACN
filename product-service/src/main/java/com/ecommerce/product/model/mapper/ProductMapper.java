package com.ecommerce.product.model.mapper;

import com.ecommerce.product.model.dto.ProductDTO;
import com.ecommerce.product.model.entity.Product;
import com.ecommerce.product.model.request.ProductCreateRequest;
import com.ecommerce.product.model.request.ProductUpdateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "images", target = "imageUrls") // Correct target field for category mapping
    ProductDTO toDTO(Product product);

    @Mapping(source = "categoryId", target = "category.id")
    Product toEntity(ProductCreateRequest productDTO);

    @Mapping(source = "categoryId", target = "category.id")
    Product toEntity(ProductUpdateRequest productDTO);
}
