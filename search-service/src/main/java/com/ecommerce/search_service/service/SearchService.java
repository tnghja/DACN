package com.ecommerce.search_service.service;


import com.ecommerce.search_service.model.entity.ProductDocument;
import com.ecommerce.search_service.model.request.ElasticSearchRequest;
import com.ecommerce.search_service.model.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;

import java.util.List;

public interface SearchService {
    Page<ProductResponse> searchProducts(String name, String category, Double minPrice, Double maxPrice, Double minRate, Double maxRate, Pageable pageable);

    List<ProductDocument> findProductsByIds(List<String> productIds);

    Page<SearchHit<ProductDocument>> elasticSearchProducts(ElasticSearchRequest request);

    List<String> autocompleteProductNames(String prefix);
}