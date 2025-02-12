package com.ecommerce.search_service.service.impl;

import com.ecommerce.search_service.model.entity.Product;
import com.ecommerce.search_service.model.entity.ProductDocument;
import com.ecommerce.search_service.model.mapper.ProductMapper;
import com.ecommerce.search_service.model.response.ProductResponse;
import com.ecommerce.search_service.repository.ProductElasticsearchRepository;
import com.ecommerce.search_service.repository.ProductRepository;
import com.ecommerce.search_service.repository.ProductSpecification;
import com.ecommerce.search_service.service.SearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductElasticsearchRepository productElasticsearchRepository;

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public Page<ProductResponse> searchProducts(String name, String categoryName, Double minPrice, Double maxPrice, Double minRate, Double maxRate, Pageable pageable) {
        Page<Product> products = productRepository.findAll(ProductSpecification.filterBy(name, categoryName, minPrice, maxPrice, minRate, maxRate), pageable);
        return products.map(productMapper::toResponse);
    }

    @Override
    public Page<ProductDocument> elasticSearchProducts(
            String name, String category, Double minPrice, Double maxPrice, Double minRate, Double maxRate, Pageable pageable) {

        // Build the criteria for the query
        Criteria criteria = new Criteria();

        if (name != null && !name.isEmpty()) {
            criteria.and(new Criteria("name").fuzzy(name));
        }

        if (category != null && !category.isEmpty()) {
            criteria.and(new Criteria("categoryName").is(category));
        }

        if (minPrice != null) {
            criteria.and(new Criteria("price").greaterThanEqual(minPrice));
        }

        if (maxPrice != null) {
            criteria.and(new Criteria("price").lessThanEqual(maxPrice));
        }

        if (minRate != null) {
            criteria.and(new Criteria("rate").greaterThanEqual(minRate));
        }

        if (maxRate != null) {
            criteria.and(new Criteria("rate").lessThanEqual(maxRate));
        }

        // Create the query with pagination
        Query query = new CriteriaQuery(criteria).setPageable(pageable);

        // Execute the search
        SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);

        // Extract the content (results for the current page)
        List<ProductDocument> content = searchHits.stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        // Get the total number of elements (for pagination)
        long totalElements = searchHits.getTotalHits();

        // Create and return a Page object
        return new PageImpl<>(content, pageable, totalElements);
    }
}