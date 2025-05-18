package com.ecommerce.recombee_service.service;

import com.ecommerce.recombee_service.model.entity.request.InteractionBatchRequest;
import com.ecommerce.recombee_service.model.entity.request.InteractionRequest;
import com.recombee.api_client.RecombeeClient;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;

public interface RecombeeService {
    void sendDetailView(InteractionRequest request);
    void sendPurchase(InteractionRequest request);
    void sendBatchInteractions(InteractionBatchRequest requests);
    List<String> getRecommendations(String userId, int count);

    List<String> getPopularItems(long count);

    List<String> getNewItems(long count);

    List<String> getFeaturedItems(long count);

    List<String> getItemsBasedOnContext(List<String> recentlyViewedItems, List<String> categories, int count);
    
    // Method to get the RecombeeClient instance for direct API access
    RecombeeClient getClient();
}