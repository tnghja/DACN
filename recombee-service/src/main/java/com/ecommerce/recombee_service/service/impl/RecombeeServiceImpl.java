package com.ecommerce.recombee_service.service.impl;

import com.ecommerce.recombee_service.exception.RecombeeException;
import com.ecommerce.recombee_service.model.entity.request.InteractionBatchRequest;
import com.ecommerce.recombee_service.model.entity.request.InteractionRequest;
import com.ecommerce.recombee_service.service.RecombeeService;
import com.recombee.api_client.RecombeeClient;
import com.recombee.api_client.api_requests.*;
import com.recombee.api_client.bindings.Recommendation;
import com.recombee.api_client.bindings.RecommendationResponse;
import com.recombee.api_client.bindings.SearchResponse;
import com.recombee.api_client.exceptions.ApiException;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Getter
public class RecombeeServiceImpl implements RecombeeService {

    private final RecombeeClient client;

    public RecombeeServiceImpl(RecombeeClient client) {
        this.client = client;
    }
    
    @Override
    public RecombeeClient getClient() {
        return this.client;
    }

    @Override
    public void sendDetailView(InteractionRequest request) {
        try {
            client.send(new AddDetailView(request.getUserId(), request.getItemId())
                    .setCascadeCreate(true));  // Automatically create users/items if they don't exist
        } catch (ApiException e) {
            throw new RecombeeException(e.getMessage());
        }
    }

    @Override
    public void sendPurchase(InteractionRequest request) {
        try {
            client.send(new AddPurchase(request.getUserId(), request.getItemId())
                    .setCascadeCreate(true));  // Automatically create users/items if they don't exist
        } catch (ApiException e) {
            throw new RecombeeException(e.getMessage());
        }
    }

    @Override
    public void sendBatchInteractions(InteractionBatchRequest requests) {
        try {
            List<Request> batch = new ArrayList<>();
            
            for (String itemId : requests.getItemIds()) {
                batch.add(new AddDetailView(requests.getUserId(), itemId)
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
    
    @Override
    public List<String> getPopularItems(long count) {
        try {
            // Use the RecommendItemsToUser with a special scenario for popular items
            // We'll create a temporary "item-discovery" user for this purpose
            String tempUserId = "items-discovery-" + UUID.randomUUID().toString();
            
            RecommendationResponse response = client.send(
                    new RecommendItemsToUser(tempUserId, count)
                            .setCascadeCreate(true)
                            .setScenario("popular_items")
            );
            
            // Cleanup the temporary user
            try {
                client.send(new DeleteUser(tempUserId));
            } catch (ApiException e) {
                // Ignore cleanup errors
            }
            
            return List.of(response.getRecomms()).stream()
                    .map(Recommendation::getId)
                    .collect(Collectors.toList());
        } catch (ApiException e) {
            throw new RecombeeException(e.getMessage());
        }
    }
    
    @Override
    public List<String> getNewItems(long count) {
        try {
            // Two approaches to get new items:
            // 1. If items have a dateAdded property, use SearchItems with a filter
            // 2. If not, use RecommendItemsToUser with a specific scenario
            
            try {
                // Try the SearchItems approach first
                SearchResponse response = client.send(
                        new SearchItems("","", count)
                                .setFilter("'now' - dateAdded < 30 days")
                                .setReturnProperties(true)
                );
                
                return List.of(response.getRecomms()).stream()
                        .map(Recommendation::getId)
                        .collect(Collectors.toList());
            } catch (ApiException e) {
                // Fallback to scenario-based approach
                String tempUserId = "new-items-" + UUID.randomUUID().toString();
                
                RecommendationResponse response = client.send(
                        new RecommendItemsToUser(tempUserId, count)
                                .setCascadeCreate(true)
                                .setScenario("new_items")
                );
                
                // Cleanup the temporary user
                try {
                    client.send(new DeleteUser(tempUserId));
                } catch (ApiException ex) {
                    // Ignore cleanup errors
                }
                
                return List.of(response.getRecomms()).stream()
                        .map(Recommendation::getId)
                        .collect(Collectors.toList());
            }
        } catch (ApiException e) {
            throw new RecombeeException(e.getMessage());
        }
    }
    
    @Override
    public List<String> getFeaturedItems(long count) {
        try {
            // We'll assume there's a boolean property called "featured" 
            // that can be used to filter featured items
            
            try {
                // Try using SearchItems with a filter for featured items
                SearchResponse response = client.send(
                        new SearchItems("", "",count)
                                .setFilter("featured = true")
                                .setReturnProperties(true)
                );
                
                return List.of(response.getRecomms()).stream()
                        .map(Recommendation::getId)
                        .collect(Collectors.toList());
            } catch (ApiException e) {
                // Fallback to scenario-based approach
                String tempUserId = "featured-items-" + UUID.randomUUID().toString();
                
                RecommendationResponse response = client.send(
                        new RecommendItemsToUser(tempUserId, count)
                                .setCascadeCreate(true)
                                .setScenario("featured_items")
                );
                
                // Cleanup the temporary user
                try {
                    client.send(new DeleteUser(tempUserId));
                } catch (ApiException ex) {
                    // Ignore cleanup errors
                }
                
                return List.of(response.getRecomms()).stream()
                        .map(Recommendation::getId)
                        .collect(Collectors.toList());
            }
        } catch (ApiException e) {
            throw new RecombeeException(e.getMessage());
        }
    }
    
    @Override
    public List<String> getItemsBasedOnContext(List<String> recentlyViewedItems, List<String> categories, int count) {
        try {
            // If we have recently viewed items, we can use them to generate temporary user session
            // and get recommendations based on those items
            String tempUserId = "anonymous-" + UUID.randomUUID();
            
            if (recentlyViewedItems != null && !recentlyViewedItems.isEmpty()) {
                List<Request> batch = new ArrayList<>();
                
                // Add view interactions for each recently viewed item
                for (String itemId : recentlyViewedItems) {
                    batch.add(new AddDetailView(tempUserId, itemId)
                            .setCascadeCreate(true));
                }
                
                // Send batch of views
                client.send(new Batch(batch));
                
                // Build filter based on categories if provided
                String filter = null;
                if (categories != null && !categories.isEmpty()) {
                    filter = categories.stream()
                            .map(category -> "'" + category + "'")
                            .collect(Collectors.joining(", ", "categories IN [", "]"));
                }
                
                // Get recommendations for this temporary user
                RecommendationResponse response = client.send(
                        new RecommendItemsToUser(tempUserId, count)
                                .setFilter(filter)
                                .setBooster("if 'now' - dateAdded < 30 days then 1.5 else 1")
                                .setReturnProperties(true)
                );
                
                // Clean up the temporary user
                try {
                    client.send(new DeleteUser(tempUserId));
                } catch (ApiException e) {
                    // Ignore cleanup errors
                }
                
                return List.of(response.getRecomms()).stream()
                        .map(Recommendation::getId)
                        .collect(Collectors.toList());
            } else if (categories != null && !categories.isEmpty()) {
                // If we have no recently viewed items but have categories, search items in these categories
                String filter = categories.stream()
                        .map(category -> "'" + category + "'")
                        .collect(Collectors.joining(", ", "categories IN [", "]"));
                
                SearchResponse response = client.send(
                        new SearchItems("","", count)
                                .setFilter(filter)
                                .setReturnProperties(true)
                );
                
                return List.of(response.getRecomms()).stream()
                        .map(Recommendation::getId)
                        .collect(Collectors.toList());
            } else {
                // Fallback to popular items if no context available
                return getPopularItems(count);
            }
        } catch (ApiException e) {
            throw new RecombeeException(e.getMessage());
        }
    }
    


}