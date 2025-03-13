package com.ecommerce.search_service.model.mapper;

import com.ecommerce.search_service.model.entity.Category;
import com.ecommerce.search_service.model.entity.Product;
import com.ecommerce.search_service.model.response.ProductResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.2 (Oracle Corporation)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public ProductResponse toResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        String categoryName = null;
        String id = null;
        String name = null;
        String brand = null;
        String cover = null;
        String description = null;
        Double price = null;
        Integer quantity = null;
        Double rate = null;

        categoryName = productCategoryName( product );
        id = product.getId();
        name = product.getName();
        brand = product.getBrand();
        cover = product.getCover();
        description = product.getDescription();
        price = product.getPrice();
        quantity = product.getQuantity();
        rate = product.getRate();

        ProductResponse productResponse = new ProductResponse( id, name, brand, cover, description, price, quantity, rate, categoryName );

        return productResponse;
    }

    private String productCategoryName(Product product) {
        if ( product == null ) {
            return null;
        }
        Category category = product.getCategory();
        if ( category == null ) {
            return null;
        }
        String name = category.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
