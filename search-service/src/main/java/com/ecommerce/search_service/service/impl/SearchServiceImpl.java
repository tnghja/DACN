package com.ecommerce.search_service.service.impl;


import com.ecommerce.search_service.model.entity.Product;
import com.ecommerce.search_service.model.mapper.ProductMapper;
import com.ecommerce.search_service.model.response.ProductResponse;
import com.ecommerce.search_service.repository.ProductRepository;
import com.ecommerce.search_service.repository.ProductSpecification;
import com.ecommerce.search_service.service.SearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductMapper productMapper;
    @Override
    public Page<ProductResponse> searchProducts(String name, String categoryName, Double minPrice, Double maxPrice, Double minRate, Double maxRate, Pageable pageable) {
        Page<Product> products = productRepository.findAll(ProductSpecification.filterBy(name, categoryName, minPrice, maxPrice, minRate, maxRate), pageable);

        return products.map(productMapper::toResponse);
    }


}

