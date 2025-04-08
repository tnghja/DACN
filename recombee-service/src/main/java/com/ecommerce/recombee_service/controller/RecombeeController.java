package com.ecommerce.recombee_service.controller;

import com.ecommerce.recombee_service.model.entity.request.InteractionRequest;
import com.ecommerce.recombee_service.model.response.ApiResponse;
import com.ecommerce.recombee_service.service.RecombeeService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RecombeeController {

    @Autowired
    private final RecombeeService recombeeService;

    @PostMapping("/view")
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
    public ResponseEntity<ApiResponse<Void>> purchaseItem(@RequestParam @NotBlank(message = "userId is required") String userId,
                                                          @RequestParam @NotBlank(message = "itemId is required") String itemId) {
        InteractionRequest request = new InteractionRequest(userId, itemId);
        recombeeService.sendPurchase(request);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/batch-purchase")
    public ResponseEntity<ApiResponse<Void>> batchPurchase(@Valid @RequestBody List<InteractionRequest> requests) {
        recombeeService.sendBatchInteractions(requests);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<Object>> getRecommendations(
            @RequestParam String userId,
            @RequestParam(defaultValue = "5") int count) {
        var recommendations = recombeeService.getRecommendations(userId, count);
        ApiResponse<Object> response = new ApiResponse<>();
        response.ok(recommendations);
        return ResponseEntity.ok(response);
    }
}