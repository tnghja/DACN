package com.ecommerce.search_service.controller;

import com.ecommerce.search_service.exception.SearchOptionsException;
import com.ecommerce.search_service.model.entity.Product;
import com.ecommerce.search_service.model.entity.ProductDocument;
import com.ecommerce.search_service.model.request.ElasticSearchRequest;
import com.ecommerce.search_service.model.request.ProductListRequest;
import com.ecommerce.search_service.model.response.ApiResponse;
import com.ecommerce.search_service.model.response.ProductResponse;
import com.ecommerce.search_service.service.ImageSearchService;
import com.ecommerce.search_service.service.SearchService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.ecommerce.search_service.constants.SortConstants.getSortMetadata;

@RestController
@RequiredArgsConstructor
//@RequestMapping("/user")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class SearchController {
    @Autowired
    SearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRate,
            @RequestParam(required = false) Double maxRate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> productPage = searchService.searchProducts(name, category, minPrice, maxPrice, minRate, maxRate, pageable);

        // Construct API Response
        ApiResponse<List<ProductResponse>> response = new ApiResponse<>();
        response.ok(productPage.getContent());
        response.setMetadata(Map.of(
                "currentPage", productPage.getNumber(),
                "totalItems", productPage.getTotalElements(),
                "totalPages", productPage.getTotalPages(),
                "pageSize", productPage.getSize()
        ));

        return ResponseEntity.ok(response);
    }
    @PostMapping("/products/batch")
    public ResponseEntity<List<ProductResponse>> getProductsByIds(@Valid @RequestBody ProductListRequest request) {
        List<ProductResponse> products = searchService.findProductsByIds(request.getProductIds());
        return ResponseEntity.ok(products);
    }
    @PostMapping("/elasticSearch")
    public ResponseEntity<ApiResponse<List<SearchHit<ProductDocument>>>> elasticSearchProducts(
            @Valid @RequestBody ElasticSearchRequest request) {

        ApiResponse<List<SearchHit<ProductDocument>>> response = new ApiResponse<>();

        // Validate minPrice <= maxPrice
        if (request.getMinPrice() != null && request.getMaxPrice() != null && request.getMinPrice() > request.getMaxPrice()) {
            throw new SearchOptionsException("minPrice cannot be greater than maxPrice");
        }

        // Validate minRate <= maxRate
        if (request.getMinRate() != null && request.getMaxRate() != null && request.getMinRate() > request.getMaxRate()) {
            throw new SearchOptionsException("minRate cannot be greater than maxRate");
        }

        // Gọi service với request
        Page<SearchHit<ProductDocument>> productPage = searchService.elasticSearchProducts(request);

        response.ok(productPage.getContent());
        response.setMetadata(Map.of(
                "currentPage", request.getPage(),
                "totalItems", productPage.getTotalElements(),
                "totalPages", productPage.getTotalPages(),
                "pageSize", productPage.getSize(),
                "sortOptions", getSortMetadata()
        ));

        return ResponseEntity.ok(response);
    }
    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<String>>> autocomplete(
            @RequestParam String prefix) {

        List<String> suggestions = searchService.autocompleteProductNames(prefix);

        ApiResponse<List<String>> response = new ApiResponse<>();
        response.ok(suggestions);
        response.setMetadata(Map.of(
                "totalSuggestions", suggestions.size()
        ));

        return ResponseEntity.ok(response);
    }
//    @PostMapping(value = "/searchImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<ApiResponse<List<ProductDocument>>> searchSimilarityImages(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam(defaultValue = "0") int page, // Page starts at 0 for Spring Pageable
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) String sort) {
//
//
//        // Step 1: Create cache key from file content
//        Pageable pageable = (sort != null)
//                ? PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sort.split(",")[1]), sort.split(",")[0]))
//                : PageRequest.of(page, size);
//
//        List<String> productIds;
//        if (cachedProductIds == null || cachedProductIds.isEmpty()) {
//            // Step 2: Extract vector and query Pinecone
//            List<String> vector = imageSearchService.getImageVector(fileContent);
//            productIds = imageSearchService.queryPinecone(vector);
//            imageSearchService.cacheProductIds(cacheKey, productIds);
//        } else {
//            productIds = new ArrayList<>(cachedProductIds);
//        }
//
//        // Step 3: Pagination
//        int totalItems = productIds.size();
//        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
//        int startIdx = (page - 1) * pageSize;
//        int endIdx = Math.min(startIdx + pageSize, totalItems);
//        List<String> paginatedProductIds = productIds.subList(startIdx, endIdx);
//
//        // Step 4: Fetch product info
//        List<ProductInfo> productInfo = imageSearchService.fetchProducts(paginatedProductIds);
//
//        // Step 5: Build response
//        ApiResponse<List<ProductInfo>> response = ApiResponse.ok(productInfo);
//        response.setMetadata(Map.of(
//                "currentPage", page,
//                "totalItems", totalItems,
//                "pageSize", pageSize,
//                "totalPages", totalPages
//        ));
//
//        return ResponseEntity.ok(response);
//
//
//    }

}
