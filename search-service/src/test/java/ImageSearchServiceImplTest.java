//package com.ecommerce.search_service.service.impl;

import com.ecommerce.search_service.exception.ImageProcessingException;
import com.ecommerce.search_service.exception.NotFoundException;
import com.ecommerce.search_service.model.entity.ProductDocument;
import com.ecommerce.search_service.model.request.ImageSearchRequest;
import com.ecommerce.search_service.model.request.ImageSessionRequest;
import com.ecommerce.search_service.model.response.ImageSearchResponse;
import com.ecommerce.search_service.service.ImageSearchService;
import com.ecommerce.search_service.service.RedisService;
import com.ecommerce.search_service.service.SearchService;
import com.ecommerce.search_service.service.impl.ImageSearchServiceImpl;
import com.ecommerce.search_service.utils.ImageHashUtil;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.clients.Index;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import io.pinecone.unsigned_indices_model.ScoredVectorWithUnsignedIndices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageSearchServiceImplTest {

    @Mock
    private WebClient webClient;

    @Mock
    private SearchService searchService;

    @Mock
    private Index pineconeIndex;

    @Mock
    private RedisService redisService;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private ImageSearchServiceImpl imageSearchService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(webClient, searchService, pineconeIndex, redisService, multipartFile);
    }
//
//    @Test
//    void testExtractImageVector_Success() throws IOException {
//        // Arrange
//        byte[] fileBytes = new byte[]{1, 2, 3};
//        Map<String, Object> responseMap = Map.of("vector", List.of(1.0, 2.0, 3.0));
//
//        when(multipartFile.getBytes()).thenReturn(fileBytes);
//        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
//
//        when(webClient.post()).thenReturn(requestBodyUriSpec);
//        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
//        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
//        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
//        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
//        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
//        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(responseMap));
//
//        // Act
//        List<Float> result = imageSearchService.extractImageVector(multipartFile);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(3, result.size());
//        assertEquals(1.0f, result.get(0));
//        assertEquals(2.0f, result.get(1));
//        assertEquals(3.0f, result.get(2));
//    }

    @Test
    void testExtractImageVector_Failure_IOException() throws IOException {
        // Arrange
        when(multipartFile.getBytes()).thenThrow(new IOException("File read error"));

        // Act & Assert
        ImageProcessingException exception = assertThrows(ImageProcessingException.class, () -> {
            imageSearchService.extractImageVector(multipartFile);
        });
        assertEquals("Error reading file: File read error", exception.getMessage());
    }
//
//    @Test
//    void testExtractImageVector_Failure_NoVector() throws IOException {
//        // Arrange
//        byte[] fileBytes = new byte[]{1, 2, 3};
//        Map<String, Object> responseMap = Map.of("error", "No vector found");
//
//        when(multipartFile.getBytes()).thenReturn(fileBytes);
//        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
//
//        when(webClient.post()).thenReturn(requestBodyUriSpec);
//        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
//        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
//        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
//        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
//        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
//        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(responseMap));
//
//        // Act & Assert
//        ImageProcessingException exception = assertThrows(ImageProcessingException.class, () -> {
//            imageSearchService.extractImageVector(multipartFile);
//        });
//        assertEquals("No vector returned from image processing service", exception.getMessage());
//    }

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
    void testSearchByImage_CachedResults() {
        // Arrange
        ImageSearchRequest request = new ImageSearchRequest(multipartFile, 1, 10);
        String imageHash = "hash123";
        List<String> cachedProductIds = List.of("prod1", "prod2", "prod3");
        List<ProductDocument> products = Arrays.asList(
                createProductDocument("prod1", "Product 1", "Description 1", 100.0),
                createProductDocument("prod2", "Product 2", "Description 2", 200.0)
        );

        when(ImageHashUtil.hashImage(multipartFile)).thenReturn(imageHash);
        when(redisService.getCachedImageSearchResults(imageHash)).thenReturn(cachedProductIds);
        when(searchService.findProductsByIds(anyList())).thenReturn(products);

        // Act
        ImageSearchResponse response = imageSearchService.searchByImage(request);

        // Assert
        assertNotNull(response);
        assertEquals(imageHash, response.getImageHash());
        assertEquals(products, response.getProducts());
        assertEquals(1, response.getMetadata().get("currentPage"));
        assertEquals(3, response.getMetadata().get("totalItems"));
        assertEquals(10, response.getMetadata().get("pageSize"));
        assertEquals(1, response.getMetadata().get("totalPages"));
    }

//    @Test
//    void testSearchByImage_NoCache() throws IOException {
//        // Arrange
//        ImageSearchRequest request = new ImageSearchRequest(multipartFile, 1, 10);
//        String imageHash = "hash123";
//        List<Float> imageVector = List.of(1.0f, 2.0f, 3.0f);
//        List<String> productIds = List.of("prod1", "prod2");
//        List<ProductDocument> products = Arrays.asList(
//                createProductDocument("prod1", "Product 1", "Description 1", 100.0),
//                createProductDocument("prod2", "Product 2", "Description 2", 200.0)
//        );
//
//        when(ImageHashUtil.hashImage(multipartFile)).thenReturn(imageHash);
//        when(redisService.getCachedImageSearchResults(imageHash)).thenReturn(null);
//        when(multipartFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
//        when(multipartFile.getOriginalFilename()).thenReturn("test.jpg");
//
//        // Mock WebClient response
//        Map<String, Object> responseMap = Map.of("vector", List.of(1.0, 2.0, 3.0));
//        when(webClient.post()).thenReturn(requestBodyUriSpec);
//        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
//        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
//        when(requestBodySpec.body(any())).thenReturn(requestBodySpec);
//        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
//        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
//        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(responseMap));
//
//        // Mock Pinecone response
//        QueryResponseWithUnsignedIndices pineconeResponse = mock(QueryResponseWithUnsignedIndices.class);
//        ScoredVectorWithUnsignedIndices vector = mock(ScoredVectorWithUnsignedIndices.class);
//        Struct metadata = Struct.newBuilder()
//                .putFields("product_id", Value.newBuilder().setStringValue("prod1").build())
//                .build();
//        when(vector.getMetadata()).thenReturn(metadata);
//        when(pineconeResponse.getMatchesList()).thenReturn(List.of(vector));
//        when(pineconeIndex.query(anyInt(), anyList(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(false), eq(true)))
//                .thenReturn(pineconeResponse);
//
//        when(searchService.findProductsByIds(anyList())).thenReturn(products);
//
//        // Act
//        ImageSearchResponse response = imageSearchService.searchByImage(request);
//
//        // Assert
//        assertNotNull(response);
//        assertEquals(imageHash, response.getImageHash());
//        assertEquals(products, response.getProducts());
//        assertEquals(1, response.getMetadata().get("currentPage"));
//        assertEquals(1, response.getMetadata().get("totalItems"));
//        assertEquals(10, response.getMetadata().get("pageSize"));
//        assertEquals(1, response.getMetadata().get("totalPages"));
//
//        // Verify that results were cached
//        verify(redisService).cacheImageSearchResults(eq(imageHash), anyList());
//    }

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