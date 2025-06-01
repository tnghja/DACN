package com.ecommerce.search_service.controller;

import com.ecommerce.search_service.exception.InvalidImageException;
import com.ecommerce.search_service.exception.SearchOptionsException;
import com.ecommerce.search_service.model.entity.ProductDocument;
import com.ecommerce.search_service.model.request.ElasticSearchRequest;
import com.ecommerce.search_service.model.request.ImageSearchRequest;
import com.ecommerce.search_service.model.request.ImageSessionRequest;
import com.ecommerce.search_service.model.request.ProductListRequest;
import com.ecommerce.search_service.model.response.ApiResponse;
import com.ecommerce.search_service.model.response.ImageSearchResponse;
import com.ecommerce.search_service.model.response.ProductResponse;
import com.ecommerce.search_service.service.ImageSearchService;
import com.ecommerce.search_service.service.RedisService;
import com.ecommerce.search_service.service.SearchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static com.ecommerce.search_service.constants.ImageConstants.ALLOWED_IMAGE_TYPES;
import static com.ecommerce.search_service.constants.SortConstants.getSortMetadata;

@RestController
@RequiredArgsConstructor
//@RequestMapping("/user")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
public class SearchController {
    @Autowired
    SearchService searchService;
    @Autowired
    ImageSearchService imageSearchService;
    @Autowired
    RedisService redisService;
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
    public ResponseEntity<List<ProductDocument>> getProductsByIds(@Valid @RequestBody ProductListRequest request) {
        List<ProductDocument> products = searchService.findProductsByIds(request.getProductIds());
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


    @PostMapping(value = "/image-search", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ImageSearchResponse>> imageSearch(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(value = "pageSize", defaultValue = "10") @Min(1) int pageSize) {
        ImageSearchRequest request = new ImageSearchRequest(file, page, pageSize);

        // Kiểm tra file có rỗng không
        if (file.isEmpty()) {
            throw new InvalidImageException("Uploaded image file is empty.");
        }

        // Kiểm tra content type của file
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new InvalidImageException("Invalid file format. Only JPEG, PNG are allowed.");
        }

        // Gọi service xử lý tìm kiếm
        ImageSearchResponse searchResponse = imageSearchService.searchByImage(request);

        // Tạo response theo format Elasticsearch API
        ApiResponse<ImageSearchResponse> response = new ApiResponse<>();
        response.ok(searchResponse);

        return ResponseEntity.ok(response);

    }

    @PostMapping("/session")
    public ResponseEntity<ApiResponse<ImageSearchResponse>> getPaginatedResults(
            @Valid @RequestBody ImageSessionRequest request) {

        // Gọi service lấy dữ liệu từ Redis và phân trang
        ImageSearchResponse productPage = imageSearchService.getPaginatedResults(request);

        ApiResponse<ImageSearchResponse> response = new ApiResponse<>();
        response.ok(productPage);

        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/image-search/cache")
    public ResponseEntity<ApiResponse<String>> invalidateImageSearchCache() {
        redisService.invalidateAllCache();

        ApiResponse<String> response = new ApiResponse<>();
        response.ok("All image search cache invalidated successfully.");
        return ResponseEntity.ok(response);
    }
}
