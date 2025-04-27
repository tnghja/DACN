package com.ecommerce.recombee_service.service;

import com.ecommerce.recombee_service.model.entity.request.InteractionBatchRequest;
import com.ecommerce.recombee_service.model.entity.request.InteractionRequest;

import java.util.List;

public interface RecombeeService {
    void sendDetailView(InteractionRequest request);
    void sendPurchase(InteractionRequest request);
    void sendBatchInteractions(InteractionBatchRequest requests);
    List<String> getRecommendations(String userId, int count);
}