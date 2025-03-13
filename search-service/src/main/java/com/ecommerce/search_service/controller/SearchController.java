package com.ecommerce.search_service.controller;

import com.ecommerce.search_service.model.entity.Product;
import com.ecommerce.search_service.model.entity.ProductDocument;
import com.ecommerce.search_service.model.response.ApiResponse;
import com.ecommerce.search_service.model.response.ProductResponse;
import com.ecommerce.search_service.service.SearchService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    @GetMapping("/elasticSearch")
    public ResponseEntity<ApiResponse<List<ProductDocument>>> elasticSearchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRate,
            @RequestParam(required = false) Double maxRate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDocument> productPage = searchService.elasticSearchProducts(name, category, minPrice, maxPrice, minRate, maxRate, pageable);

        ApiResponse<List<ProductDocument>> response = new ApiResponse<>();
        response.ok(productPage.getContent());
        response.setMetadata(Map.of(
                "currentPage", productPage.getNumber(),
                "totalItems", productPage.getTotalElements(),
                "totalPages", productPage.getTotalPages(),
                "pageSize", productPage.getSize()
        ));

        return ResponseEntity.ok(response);
    }


}
