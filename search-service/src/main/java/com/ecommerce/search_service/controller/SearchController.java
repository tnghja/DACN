package com.ecommerce.search_service.controller;

import com.ecommerce.search_service.model.entity.Product;
import com.ecommerce.search_service.model.entity.ProductDocument;
import com.ecommerce.search_service.model.request.ProductListRequest;
import com.ecommerce.search_service.model.response.ApiResponse;
import com.ecommerce.search_service.model.response.ProductResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
//    @GetMapping("/elasticSearch")
//    public ResponseEntity<ApiResponse<List<ProductDocument>>> elasticSearchProducts(
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) Long categoryId,
//            @RequestParam(required = false) Double minPrice,
//            @RequestParam(required = false) Double maxPrice,
//            @RequestParam(required = false) Double minRate,
//            @RequestParam(required = false) Double maxRate,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        Pageable pageable = PageRequest.of(page, size);
//        Page<ProductDocument> productPage = searchService.elasticSearchProducts(name, categoryId, minPrice, maxPrice, minRate, maxRate, pageable);
//
//        ApiResponse<List<ProductDocument>> response = new ApiResponse<>();
//        response.ok(productPage.getContent());
//        response.setMetadata(Map.of(
//                "currentPage", productPage.getNumber(),
//                "totalItems", productPage.getTotalElements(),
//                "totalPages", productPage.getTotalPages(),
//                "pageSize", productPage.getSize()
//        ));
//
//        return ResponseEntity.ok(response);
//    }
    @PostMapping("/products/batch")
    public ResponseEntity<List<ProductResponse>> getProductsByIds(@Valid @RequestBody ProductListRequest request) {
        List<ProductResponse> products = searchService.findProductsByIds(request.getProductIds());
        return ResponseEntity.ok(products);
    }
    private static final List<String> AVAILABLE_SORT_FIELDS = List.of("price", "rate", "quantity");
    private Map<String, Object> getSortMetadata() {
        return Map.of(
                "availableFields", AVAILABLE_SORT_FIELDS,
                "syntax", "field,direction (e.g., price,asc or rate,desc)",
                "directions", List.of("asc", "desc")
        );
    }
    @GetMapping("/elasticSearch")
    public ResponseEntity<ApiResponse<List<SearchHit<ProductDocument>>>> elasticSearchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRate,
            @RequestParam(required = false) Double maxRate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort) {

        Pageable pageable;
        ApiResponse<List<SearchHit<ProductDocument>>> response = new ApiResponse<>();

        // Xử lý sort từ query parameter
        if (sort != null) {
            try {
                String[] sortParts = sort.split(",");
                if (sortParts.length != 2) {
                    response.error(
                            Map.of("sort", "Invalid sort format"),
                            Map.of("details", "Use 'field,direction' (e.g., price,asc)")
                    );
                    response.setMetadata(getSortMetadata());
                    return ResponseEntity.badRequest().body(response);
                }

                String field = sortParts[0];
                String direction = sortParts[1];

                // Kiểm tra field có hợp lệ không
                if (!AVAILABLE_SORT_FIELDS.contains(field)) {
                    response.error(
                            Map.of("sort", "Invalid sort field: " + field),
                            Map.of("availableFields", AVAILABLE_SORT_FIELDS)
                    );
                    response.setMetadata(getSortMetadata());
                    return ResponseEntity.badRequest().body(response);
                }

                // Kiểm tra direction có hợp lệ không
                if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
                    response.error(
                            Map.of("sort", "Invalid sort direction: " + direction),
                            Map.of("directions", List.of("asc", "desc"))
                    );
                    response.setMetadata(getSortMetadata());
                    return ResponseEntity.badRequest().body(response);
                }

                pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), field));
            } catch (Exception e) {
                response.error(
                        Map.of("sort", "Error parsing sort parameter"),
                        Map.of("errorDetails", e.getMessage())
                );
                response.setMetadata(getSortMetadata());
                return ResponseEntity.badRequest().body(response);
            }
        } else {
            pageable = PageRequest.of(page, size);
        }

        Page<SearchHit<ProductDocument>> productPage = searchService.elasticSearchProducts(
                name, categoryId, minPrice, maxPrice, minRate, maxRate, pageable
        );

        response.ok(productPage.getContent());
        response.setMetadata(Map.of(
                "currentPage", productPage.getNumber(),
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

}
