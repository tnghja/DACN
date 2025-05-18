package com.ecommerce.recombee_service.controller;

import com.ecommerce.recombee_service.model.entity.request.ContextRequest;
import com.ecommerce.recombee_service.model.entity.request.InteractionBatchRequest;
import com.ecommerce.recombee_service.model.entity.request.InteractionRequest;
import com.ecommerce.recombee_service.model.response.ApiResponse;
import com.ecommerce.recombee_service.service.RecombeeService;
import com.recombee.api_client.api_requests.RecommendItemsToItem;
import com.recombee.api_client.api_requests.SearchItems;
import com.recombee.api_client.bindings.Recommendation;
import com.recombee.api_client.bindings.RecommendationResponse;
import com.recombee.api_client.bindings.SearchResponse;
import com.recombee.api_client.exceptions.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Recombee API", description = "Operations for recommendation service")
public class RecombeeController {

    @Autowired
    private final RecombeeService recombeeService;

    @PostMapping("/view")
    @Operation(summary = "Record a view interaction")
    public ResponseEntity<ApiResponse<Void>> viewItem(
            @RequestParam @NotBlank(message = "userId is required") String userId,
            @RequestParam @NotBlank(message = "itemId is required") String itemId) {
        InteractionRequest request = new InteractionRequest(userId, itemId);
        recombeeService.sendDetailView(request);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/purchase")
    @Operation(summary = "Record a purchase interaction")
    public ResponseEntity<ApiResponse<Void>> purchaseItem(
            @RequestParam @NotBlank(message = "userId is required") String userId,
            @RequestParam @NotBlank(message = "itemId is required") String itemId) {
        InteractionRequest request = new InteractionRequest(userId, itemId);
        recombeeService.sendPurchase(request);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch-purchase")
    public ResponseEntity<ApiResponse<Void>> batchPurchase(@Valid @RequestBody InteractionBatchRequest requests) {
        recombeeService.sendBatchInteractions(requests);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommend")
    @Operation(summary = "Get personalized recommendations for a user")
    public ResponseEntity<ApiResponse<Object>> getRecommendations(
            @RequestParam String userId,
            @RequestParam(defaultValue = "5") int count) {
        var recommendations = recombeeService.getRecommendations(userId, count);
        ApiResponse<Object> response = new ApiResponse<>();
        response.ok(recommendations);
        return ResponseEntity.ok(response);
    }
    
    // Endpoints for non-signed-in users
    
    @GetMapping("/popular")
    @Operation(summary = "Get popular items for non-signed-in users")
    public ResponseEntity<ApiResponse<Object>> getPopularItems(
            @RequestParam(defaultValue = "5") int count) {
        var recommendations = recombeeService.getPopularItems(count);
        ApiResponse<Object> response = new ApiResponse<>();
        response.ok(recommendations);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/new-items")
    @Operation(summary = "Get new items for non-signed-in users")
    public ResponseEntity<ApiResponse<Object>> getNewItems(
            @RequestParam(defaultValue = "5") int count) {
        var recommendations = recombeeService.getNewItems(count);
        ApiResponse<Object> response = new ApiResponse<>();
        response.ok(recommendations);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/featured")
    @Operation(summary = "Get featured items for non-signed-in users")
    public ResponseEntity<ApiResponse<Object>> getFeaturedItems(
            @RequestParam(defaultValue = "5") int count) {
        var recommendations = recombeeService.getFeaturedItems(count);
        ApiResponse<Object> response = new ApiResponse<>();
        response.ok(recommendations);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/context-based")
    @Operation(summary = "Get recommendations based on browsing context for non-signed-in users")
    public ResponseEntity<ApiResponse<Object>> getContextBasedRecommendations(
            @Valid @RequestBody ContextRequest context,
            @RequestParam(defaultValue = "5") int count) {
        var recommendations = recombeeService.getItemsBasedOnContext(
                context.getRecentlyViewedItems(), 
                context.getCategories(), 
                count);
        ApiResponse<Object> response = new ApiResponse<>();
        response.ok(recommendations);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommend-similar-items")
    @Operation(summary = "Get items similar to a specific item")
    public ResponseEntity<ApiResponse<Object>> recommendSimilarItems(
            @RequestParam @NotBlank(message = "itemId is required") String itemId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "5") int count) {
        try {
            // Create request for item-to-item recommendations
            RecommendItemsToItem request = new RecommendItemsToItem(itemId, userId, count)
                    .setCascadeCreate(true);
            
            // Apply optional filter
            if (filter != null && !filter.trim().isEmpty()) {
                request.setFilter(filter);
            }
            
            // Send request and get response
            RecommendationResponse response = recombeeService.getClient().send(request);
            
            // Extract item IDs from recommendations
            List<String> similarItems = List.of(response.getRecomms()).stream()
                    .map(Recommendation::getId)
                    .collect(Collectors.toList());
            
            // Return successful response
            ApiResponse<Object> apiResponse = new ApiResponse<>();
            apiResponse.ok(similarItems);
            return ResponseEntity.ok(apiResponse);
        } catch (ApiException e) {
            // Handle Recombee API exception
            ApiResponse<Object> errorResponse = new ApiResponse<>();
            Map<String, String> errorMap = Map.of("message", "Failed to get similar items: " + e.getMessage());
            errorResponse.error(errorMap);
            return ResponseEntity.status(500).body(errorResponse);
        } catch (Exception e) {
            // Handle any other unexpected exceptions
            ApiResponse<Object> errorResponse = new ApiResponse<>();
            Map<String, String> errorMap = Map.of("message", "Unexpected error: " + e.getMessage());
            errorResponse.error(errorMap);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}