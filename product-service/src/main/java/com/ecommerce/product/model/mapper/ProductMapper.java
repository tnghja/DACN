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

    @Mapping(target = "categoryId", expression = "java(product.getCategory().getId() != null ? product.getCategory().getId() : null)")
    @Mapping(target = "categoryName", expression = "java(product.getCategory() != null ? product.getCategory().getName() : null)")
//    @Mapping(target = "imageUrls", expression = "java(product.getImages().stream().map(Image::getUrl).toList())") // Correct target field for category mapping
    ProductDTO toDTO(Product product);

    @Mapping(source = "categoryId", target = "category.id")
    Product toEntity(ProductCreateRequest productDTO);

    @Mapping(source = "categoryId", target = "category.id")
    Product toEntity(ProductUpdateRequest productDTO);
}
