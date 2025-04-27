package com.ecommerce.recombee_service.service.impl;

import com.ecommerce.recombee_service.exception.RecombeeException;
import com.ecommerce.recombee_service.model.entity.request.InteractionBatchRequest;
import com.ecommerce.recombee_service.model.entity.request.InteractionRequest;
import com.recombee.api_client.RecombeeClient;
import com.recombee.api_client.api_requests.AddDetailView;
import com.recombee.api_client.api_requests.AddPurchase;
import com.recombee.api_client.api_requests.Batch;
import com.recombee.api_client.api_requests.RecommendItemsToUser;
import com.recombee.api_client.bindings.Recommendation;
import com.recombee.api_client.bindings.RecommendationResponse;
import com.recombee.api_client.exceptions.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecombeeServiceImplTest {

    @Mock
    private RecombeeClient recombeeClient;

    @InjectMocks
    private RecombeeServiceImpl recombeeService;

    private InteractionRequest interactionRequest;
    private InteractionBatchRequest batchRequests;

    @BeforeEach
    void setUp() {
        interactionRequest = new InteractionRequest("user123", "item456");
        batchRequests = new InteractionBatchRequest(
                "user123",
                Arrays.asList(
                        "item456",
                        "item789"
                )
        );
    }

    @Test
    void testSendDetailView_Success() throws ApiException {
        // Arrange
        when(recombeeClient.send(any(AddDetailView.class))).thenReturn(null);

        // Act
        recombeeService.sendDetailView(interactionRequest);

        // Assert
        verify(recombeeClient).send(any(AddDetailView.class));
    }

    @Test
    void testSendDetailView_Failure() throws ApiException {
        // Arrange
        when(recombeeClient.send(any(AddDetailView.class)))
                .thenThrow(new ApiException("API Error"));

        // Act & Assert
        RecombeeException exception = assertThrows(RecombeeException.class, () -> {
            recombeeService.sendDetailView(interactionRequest);
        });
        assertEquals("API Error", exception.getMessage());
    }

    @Test
    void testSendPurchase_Success() throws ApiException {
        // Arrange
        when(recombeeClient.send(any(AddPurchase.class))).thenReturn(null);

        // Act
        recombeeService.sendPurchase(interactionRequest);

        // Assert
        verify(recombeeClient).send(any(AddPurchase.class));
    }

    @Test
    void testSendPurchase_Failure() throws ApiException {
        // Arrange
        when(recombeeClient.send(any(AddPurchase.class)))
                .thenThrow(new ApiException("API Error"));

        // Act & Assert
        RecombeeException exception = assertThrows(RecombeeException.class, () -> {
            recombeeService.sendPurchase(interactionRequest);
        });
        assertEquals("API Error", exception.getMessage());
    }

    @Test
    void testSendBatchInteractions_Success() throws ApiException {
        // Arrange
        when(recombeeClient.send(any(Batch.class))).thenReturn(null);

        // Act
        recombeeService.sendBatchInteractions(batchRequests);

        // Assert
        verify(recombeeClient).send(any(Batch.class));
    }

    @Test
    void testSendBatchInteractions_Failure() throws ApiException {
        // Arrange
        when(recombeeClient.send(any(Batch.class)))
                .thenThrow(new ApiException("API Error"));

        // Act & Assert
        RecombeeException exception = assertThrows(RecombeeException.class, () -> {
            recombeeService.sendBatchInteractions(batchRequests);
        });
        assertEquals("API Error", exception.getMessage());
    }

    @Test
    void testGetRecommendations_Success() throws ApiException {
        // Arrange
        RecommendationResponse mockResponse = mock(RecommendationResponse.class);
        Recommendation[] recommendations = new Recommendation[2];
        recommendations[0] = mock(Recommendation.class);
        recommendations[1] = mock(Recommendation.class);
        
        when(recommendations[0].getId()).thenReturn("item1");
        when(recommendations[1].getId()).thenReturn("item2");
        when(mockResponse.getRecomms()).thenReturn(recommendations);
        when(recombeeClient.send(any(RecommendItemsToUser.class))).thenReturn(mockResponse);

        // Act
        List<String> result = recombeeService.getRecommendations("user123", 2);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("item1", result.get(0));
        assertEquals("item2", result.get(1));
        verify(recombeeClient).send(any(RecommendItemsToUser.class));
    }

    @Test
    void testGetRecommendations_Failure() throws ApiException {
        // Arrange
        when(recombeeClient.send(any(RecommendItemsToUser.class)))
                .thenThrow(new ApiException("API Error"));

        // Act & Assert
        RecombeeException exception = assertThrows(RecombeeException.class, () -> {
            recombeeService.getRecommendations("user123", 2);
        });
        assertEquals("API Error", exception.getMessage());
    }
} 