package com.ecommerce.product.controller;

import com.ecommerce.product.model.response.ApiResponse;
import com.ecommerce.product.model.response.MetadataResponse;
import com.ecommerce.product.model.response.RatingCrudResponse;
import com.ecommerce.product.model.response.RatingResponse;
import com.ecommerce.product.model.request.RatingRequest;
import com.ecommerce.product.service.RatingService;
import com.ecommerce.product.exception.ApplicationException;
import com.ecommerce.product.exception.NotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<RatingCrudResponse>> createRating(@RequestBody RatingRequest ratingRequest) {
        ApiResponse<RatingCrudResponse> response = new ApiResponse<>();
        response.ok(ratingService.createRating(ratingRequest));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<RatingCrudResponse>> updateRating(@RequestBody RatingRequest ratingRequest) {
        ApiResponse<RatingCrudResponse> response = new ApiResponse<>();
        response.ok(ratingService.updateRating(ratingRequest));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<RatingResponse>> getRatingByUserIdAndProductId(
            @RequestParam("userId") Long userId,
            @RequestParam("productId") String productId) {
        ApiResponse<RatingResponse> response = new ApiResponse<>();
        response.ok(ratingService.getRatingByUserIdAndProductId(userId, productId));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = {"", "/list"})
    public ResponseEntity<ApiResponse<List<RatingResponse>>> getRatingsByProduct(
            @RequestParam("productId") String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<RatingResponse> ratingPage = ratingService.getRatingsByProductId(productId, page, size);

            if (ratingPage.isEmpty()) {
                throw new NotFoundException("No ratings found for this product.");
            }

            MetadataResponse metadata = new MetadataResponse(
                    ratingPage.getTotalElements(),
                    ratingPage.getTotalPages(),
                    ratingPage.getNumber(),
                    ratingPage.getSize(),
                    (ratingPage.hasNext() ? "/api/ratings/list?productId=" + productId + "&page=" + (ratingPage.getNumber() + 1) : null),
                    (ratingPage.hasPrevious() ? "/api/ratings/list?productId=" + productId + "&page=" + (ratingPage.getNumber() - 1) : null),
                    "/api/ratings/list?productId=" + productId + "&page=" + (ratingPage.getTotalPages() - 1),
                    "/api/ratings/list?productId=" + productId + "&page=0"
            );

            ApiResponse<List<RatingResponse>> apiResponse = new ApiResponse<>();
            Map<String, Object> responseMetadata = new HashMap<>();
            responseMetadata.put("pagination", metadata);
            apiResponse.ok(ratingPage.getContent(), responseMetadata);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApplicationException();
        }
    }
}
