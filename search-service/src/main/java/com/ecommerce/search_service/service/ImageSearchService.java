package com.ecommerce.search_service.service;

import com.ecommerce.search_service.model.request.ImageSearchRequest;
import com.ecommerce.search_service.model.request.ImageSessionRequest;
import com.ecommerce.search_service.model.response.ImageSearchResponse;
import jakarta.validation.Valid;

public interface ImageSearchService {

    ImageSearchResponse searchByImage(@Valid ImageSearchRequest request);

    ImageSearchResponse getPaginatedResults(@Valid ImageSessionRequest request);
}
