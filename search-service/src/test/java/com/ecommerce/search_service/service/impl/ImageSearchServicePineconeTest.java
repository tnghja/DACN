package com.ecommerce.search_service.service.impl;

import com.ecommerce.search_service.exception.ImageProcessingException;
import com.ecommerce.search_service.exception.NotFoundException;
import com.ecommerce.search_service.model.entity.ProductDocument;
import com.ecommerce.search_service.model.request.ImageSessionRequest;
import com.ecommerce.search_service.model.response.ImageSearchResponse;
import com.ecommerce.search_service.service.ImageVectorService;
import com.ecommerce.search_service.service.RedisService;
import com.ecommerce.search_service.service.SearchService;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.clients.Index;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageSearchServicePineconeTest {

    @Mock
    private ImageVectorService imageVectorService;

    @Mock
    private SearchService searchService;

    @Mock
    private Index pineconeIndex;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private ImageSearchServiceImpl imageSearchService;

    @Test
    void testSearchSimilarProducts_Success() {
        // Arrange
        List<Float> imageVector = List.of(1.0f, 2.0f, 3.0f);
        QueryResponseWithUnsignedIndices response = mock(QueryResponseWithUnsignedIndices.class);
        ScoredVectorWithUnsignedIndices vector1 = mock(ScoredVectorWithUnsignedIndices.class);
        ScoredVectorWithUnsignedIndices vector2 = mock(ScoredVectorWithUnsignedIndices.class);
        
        Struct metadata1 = Struct.newBuilder()
                .putFields("product_id", Value.newBuilder().setStringValue("prod1").build())
                .build();
        Struct metadata2 = Struct.newBuilder()
                .putFields("product_id", Value.newBuilder().setStringValue("prod2").build())
                .build();

        when(vector1.getMetadata()).thenReturn(metadata1);
        when(vector2.getMetadata()).thenReturn(metadata2);
        when(response.getMatchesList()).thenReturn(List.of(vector1, vector2));
        
        when(pineconeIndex.query(
                eq(30), // TOP_K
                eq(imageVector),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(false),
                eq(true)
        )).thenReturn(response);

        // Act
        List<String> result = imageSearchService.searchSimilarProducts(imageVector);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("prod1", result.get(0));
        assertEquals("prod2", result.get(1));

        // Verify Pinecone interaction
        verify(pineconeIndex).query(
                eq(30),
                eq(imageVector),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(false),
                eq(true)
        );
    }

    @Test
    void testSearchSimilarProducts_EmptyMetadata() {
        // Arrange
        List<Float> imageVector = List.of(1.0f, 2.0f, 3.0f);
        QueryResponseWithUnsignedIndices response = mock(QueryResponseWithUnsignedIndices.class);
        ScoredVectorWithUnsignedIndices vector = mock(ScoredVectorWithUnsignedIndices.class);
        
        when(vector.getMetadata()).thenReturn(null); // Test null metadata
        when(response.getMatchesList()).thenReturn(List.of(vector));
        
        when(pineconeIndex.query(
                eq(30),
                eq(imageVector),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(false),
                eq(true)
        )).thenReturn(response);

        // Act
        List<String> result = imageSearchService.searchSimilarProducts(imageVector);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchSimilarProducts_Failure() {
        // Arrange
        List<Float> imageVector = List.of(1.0f, 2.0f, 3.0f);
        when(pineconeIndex.query(
                eq(30),
                eq(imageVector),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(false),
                eq(true)
        )).thenThrow(new RuntimeException("Pinecone error"));

        // Act & Assert
        ImageProcessingException exception = assertThrows(ImageProcessingException.class, () -> {
            imageSearchService.searchSimilarProducts(imageVector);
        });
        assertEquals("Error searching similar products: Pinecone error", exception.getMessage());
    }

    @Test
    void testGetPaginatedResults_Success() {
        // Arrange
        ImageSessionRequest request = new ImageSessionRequest("hash123", 2, 2);
        List<String> allProductIds = List.of("prod1", "prod2", "prod3", "prod4");
        List<ProductDocument> products = Arrays.asList(
                createProductDocument("prod3", "Product 3", "Description 3", 300.0),
                createProductDocument("prod4", "Product 4", "Description 4", 400.0)
        );

        when(redisService.getCachedImageSearchResults("hash123")).thenReturn(allProductIds);
        when(searchService.findProductsByIds(List.of("prod3", "prod4"))).thenReturn(products);

        // Act
        ImageSearchResponse response = imageSearchService.getPaginatedResults(request);

        // Assert
        assertNotNull(response);
        assertEquals("hash123", response.getImageHash());
        assertEquals(products, response.getProducts());
        assertEquals(2, response.getMetadata().get("currentPage"));
        assertEquals(4, response.getMetadata().get("totalItems"));
        assertEquals(2, response.getMetadata().get("pageSize"));
        assertEquals(2, response.getMetadata().get("totalPages"));

        // Verify Redis and Search service interactions
        verify(redisService).getCachedImageSearchResults("hash123");
        verify(searchService).findProductsByIds(List.of("prod3", "prod4"));
    }

    @Test
    void testGetPaginatedResults_LastPage() {
        // Arrange
        ImageSessionRequest request = new ImageSessionRequest("hash123", 3, 2);
        List<String> allProductIds = List.of("prod1", "prod2", "prod3", "prod4", "prod5");
        List<ProductDocument> products = Arrays.asList(
                createProductDocument("prod5", "Product 5", "Description 5", 500.0)
        );

        when(redisService.getCachedImageSearchResults("hash123")).thenReturn(allProductIds);
        when(searchService.findProductsByIds(List.of("prod5"))).thenReturn(products);

        // Act
        ImageSearchResponse response = imageSearchService.getPaginatedResults(request);

        // Assert
        assertNotNull(response);
        assertEquals("hash123", response.getImageHash());
        assertEquals(products, response.getProducts());
        assertEquals(3, response.getMetadata().get("currentPage"));
        assertEquals(5, response.getMetadata().get("totalItems"));
        assertEquals(2, response.getMetadata().get("pageSize"));
        assertEquals(3, response.getMetadata().get("totalPages"));
    }

    @Test
    void testGetPaginatedResults_Failure_SessionExpired() {
        // Arrange
        ImageSessionRequest request = new ImageSessionRequest("hash123", 1, 10);
        when(redisService.getCachedImageSearchResults("hash123")).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            imageSearchService.getPaginatedResults(request);
        });
        assertEquals("Session expired or image hash not found.", exception.getMessage());

        // Verify Redis interaction
        verify(redisService).getCachedImageSearchResults("hash123");
        verifyNoInteractions(searchService); // Ensure search service is not called
    }

    private ProductDocument createProductDocument(String id, String name, String description, double price) {
        ProductDocument product = new ProductDocument();
        product.setId(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        return product;
    }
} 