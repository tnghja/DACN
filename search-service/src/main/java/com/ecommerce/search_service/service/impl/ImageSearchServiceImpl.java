//package com.ecommerce.search_service.service.impl;
//
//import com.ecommerce.search_service.model.entity.ProductDocument;
//import com.ecommerce.search_service.service.ImageSearchService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//
//import java.util.List;
//import java.util.Set;
//
//public class ImageSearchServiceImpl implements ImageSearchService {
//
//    private final String PINECONE_API_KEY = "pcsk_KUtgS_DZYC6qjTGi8VoK2rEEX99z41P48ug7oX2gAM6XbJvMMqm8aRkyfmzEfHVXeRpLB";
//    private final String PINECONE_INDEX_NAME = "image-search";
//    private final String EXTRACT_VECTOR_URL = "http://localhost:8000/extract-vector"; // FastAPI endpoint
//    private final String SEARCH_SERVICE_URL = "http://localhost:8081/search-service/products/batch";
//    private static final int TOP_K = 30;
//    @Override
//    public List<String> getImageVector(byte[] imageBytes) throws Exception {
//
//    }
//
//    @Override
//    public List<String> queryPinecone(List<String> vector) {
//        return List.of();
//    }
//
//    @Override
//    public Page<ProductDocument> fetchProducts(List<String> productIds) {
//        return null;
//    }
//
//    @Override
//    public Set<String> getCachedProductIds(String cacheKey) {
//        return Set.of();
//    }
//
//    @Override
//    public void cacheProductIds(String cacheKey, List<String> productIds) {
//
//    }
//}
