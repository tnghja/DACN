package com.ecommerce.search_service.model.response;

import com.ecommerce.search_service.model.entity.ProductDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
public class ImageSearchResponse {
    private String imageHash;
    private List<ProductDocument> products;
    private Map<String, Object> metadata;


}