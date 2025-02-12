package com.ecommerce.search_service.repository;

import com.ecommerce.search_service.model.entity.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {

}