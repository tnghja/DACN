package com.ecommerce.recombee_service.service.impl;

import com.ecommerce.recombee_service.exception.RecombeeException;
import com.ecommerce.recombee_service.model.entity.request.InteractionRequest;
import com.ecommerce.recombee_service.service.RecombeeService;
import com.recombee.api_client.RecombeeClient;
import com.recombee.api_client.api_requests.*;
import com.recombee.api_client.bindings.Recommendation;
import com.recombee.api_client.bindings.RecommendationResponse;
import com.recombee.api_client.exceptions.ApiException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecombeeServiceImpl implements RecombeeService {

    private final RecombeeClient client;

    public RecombeeServiceImpl(RecombeeClient client) {
        this.client = client;
    }

    public void sendDetailView(InteractionRequest request) {
        try {
            client.send(new AddDetailView(request.getUserId(), request.getItemId())
                    .setCascadeCreate(true));  // Automatically create users/items if they don't exist
        } catch (ApiException e) {
            throw new RecombeeException(e.getMessage());
        }
    }

    public void sendPurchase(InteractionRequest request) {
        try {
            client.send(new AddPurchase(request.getUserId(), request.getItemId())
                    .setCascadeCreate(true));  // Automatically create users/items if they don't exist
        } catch (ApiException e) {
            throw new RecombeeException(e.getMessage());
        }
    }

    public void sendBatchInteractions(List<InteractionRequest> requests) {
        try {
            List<Request> batch = new ArrayList<>();
            
            for (InteractionRequest request : requests) {
                batch.add(new AddPurchase(request.getUserId(), request.getItemId())
                        .setCascadeCreate(true));
            }
            
            client.send(new Batch(batch));
        } catch (ApiException e) {
            throw new RecombeeException(e.getMessage());
        }
    }

    @Override
    public List<String> getRecommendations(String userId, int count) {
        try {
            RecommendationResponse response = client.send(
                    new RecommendItemsToUser(userId, count)
                            .setCascadeCreate(true)  // Create the user if it doesn't exist
            );
            
            return List.of(response.getRecomms()).stream()
                    .map(Recommendation::getId)
                    .collect(Collectors.toList());
        } catch (ApiException e) {
            throw new RecombeeException(e.getMessage());
        }
    }
}