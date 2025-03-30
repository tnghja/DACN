package com.ecommerce.search_service.service;

import java.util.List;
import java.util.Set;

import com.ecommerce.search_service.model.entity.ProductDocument;
import org.springframework.data.domain.Page;
public interface ImageSearchService {
    public List<String> getImageVector(byte[] imageBytes);
    public List<String> queryPinecone(List<String> vector);
    public Page<ProductDocument> fetchProducts(List<String> productIds);
    public Set<String> getCachedProductIds(String cacheKey);
    public void cacheProductIds(String cacheKey, List<String> productIds);
}
