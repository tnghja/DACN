package com.ecommerce.search_service.service;

import com.ecommerce.search_service.exception.ImageProcessingException;
import com.ecommerce.search_service.exception.NotFoundException;
import com.ecommerce.search_service.model.request.ImageSearchRequest;
import com.ecommerce.search_service.model.request.ImageSessionRequest;
import com.ecommerce.search_service.model.response.ImageSearchResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageSearchService {
    List<Float> extractImageVector(MultipartFile file);
    List<String> searchSimilarProducts(List<Float> imageVector);
    ImageSearchResponse searchByImage(ImageSearchRequest request);
    ImageSearchResponse getPaginatedResults(ImageSessionRequest request);
}
