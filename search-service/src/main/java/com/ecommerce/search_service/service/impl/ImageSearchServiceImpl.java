package com.ecommerce.search_service.service.impl;

import com.ecommerce.search_service.exception.ImageProcessingException;
import com.ecommerce.search_service.exception.NotFoundException;
import com.ecommerce.search_service.model.entity.ProductDocument;
import com.ecommerce.search_service.model.request.ImageSearchRequest;
import com.ecommerce.search_service.model.request.ImageSessionRequest;
import com.ecommerce.search_service.model.response.ImageSearchResponse;
import com.ecommerce.search_service.service.ImageSearchService;
import com.ecommerce.search_service.service.RedisService;
import com.ecommerce.search_service.service.SearchService;
import com.ecommerce.search_service.utils.ImageHashUtil;
import com.google.protobuf.Struct;
import io.pinecone.clients.Index;
import io.pinecone.unsigned_indices_model.QueryResponseWithUnsignedIndices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageSearchServiceImpl implements ImageSearchService {


    private static final int TOP_K = 30;
    private final String EXTRACT_VECTOR_URL = "http://localhost:8000/extract-vector"; // FastAPI endpoint
    private final WebClient webClient;
    private final SearchService searchService;

    private final Index pineconeIndex;
    private final RedisService redisService;

    @Autowired
    public ImageSearchServiceImpl(WebClient webClient, SearchService searchService, Index pineconeIndex, RedisService redisService) {
        this.webClient = webClient;
        this.searchService = searchService;

        this.pineconeIndex = pineconeIndex;
        this.redisService = redisService;
    }

    public List<Float> extractImageVector(MultipartFile file) {
        try {
            // Chuyển file thành byte array
            byte[] fileBytes = file.getBytes();

            // Dùng MultipartBodyBuilder để tạo request body
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("file", new ByteArrayResource(fileBytes))
                    .header("Content-Disposition", "form-data; name=file; filename=" + file.getOriginalFilename());

            Map<String, Object> response = webClient.post()
                    .uri(EXTRACT_VECTOR_URL) // URL của API vector extraction
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build())) // Sử dụng MultipartBodyBuilder
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(errorMessage -> Mono.error(new ImageProcessingException("Image processing failed: " + errorMessage)))
                    )
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block(); // Blocking call (có thể thay bằng async)

            // Lấy vector từ response
            if (response == null || !response.containsKey("vector")) {
                throw new ImageProcessingException("No vector returned from image processing service");
            }
            List<Double> vectorDoubles = (List<Double>) response.get("vector");
            List<Float> vectorFloats = vectorDoubles.stream()
                    .map(Double::floatValue)  // Chuyển từng phần tử từ Double -> Float
                    .collect(Collectors.toList());
            return vectorFloats;
        } catch (IOException e) {
            throw new ImageProcessingException("Error reading file: " + e.getMessage());
        }
    }

    public List<String> searchSimilarProducts(List<Float> imageVector) {
        try {
            QueryResponseWithUnsignedIndices response = pineconeIndex.query(
                    TOP_K, // topK
                    imageVector,
                    null, // filter
                    null, // id
                    null, // includeValues
                    null, // namespace
                    null, // sparseValues
                    false, // includeVectors
                    true // includeMetadata
            );

            // Lấy danh sách sản phẩm từ metadata "product_id"
            return response.getMatchesList().stream()
                    .map(match -> {
                        Struct metadata = match.getMetadata();
                        if (metadata != null && metadata.getFieldsMap().containsKey("product_id")) {
                            return metadata.getFieldsMap().get("product_id").getStringValue();
                        }
                        return "";
                    })
                    .filter(productId -> !productId.isEmpty()) // Bỏ qua giá trị rỗng
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ImageProcessingException("Error searching similar products: " + e.getMessage());
        }
    }

    @Override
    public ImageSearchResponse searchByImage(ImageSearchRequest request) {
        String imageHash = ImageHashUtil.hashImage(request.getFile());
        List<String> cachedProductIds = redisService.getCachedImageSearchResults(imageHash);

        List<String> allProductIds;
        if (cachedProductIds != null) {
            allProductIds = cachedProductIds;
        } else {
            List<Float> imageVector = extractImageVector(request.getFile());
            allProductIds = searchSimilarProducts(imageVector);
            redisService.cacheImageSearchResults(imageHash, allProductIds);
        }

// Xử lý phân trang an toàn
        int startIdx = (request.getPage() - 1) * request.getPageSize();
        int endIdx = Math.min(startIdx + request.getPageSize(), allProductIds.size());
        List<String> paginatedProductIds = allProductIds.subList(startIdx, endIdx);

// Truy vấn Elasticsearch
        List<ProductDocument> products = searchService.findProductsByIds(paginatedProductIds);

// Trả về dữ liệu đã phân trang
        Map<String, Object> metadata = Map.of(
                "currentPage", request.getPage(),
                "totalItems", allProductIds.size(),
                "pageSize", request.getPageSize(),
                "totalPages", (int) Math.ceil((double) allProductIds.size() / request.getPageSize())
        );

        // Bước 5: Trả về response có mã hash ảnh
        return new ImageSearchResponse(imageHash, products, metadata);
    }

    @Override
    public ImageSearchResponse getPaginatedResults(ImageSessionRequest request) {
        // Lấy danh sách productId từ Redis dựa trên imageHash
        List<String> allProductIds = redisService.getCachedImageSearchResults(request.getImageHash());

        // Nếu không tìm thấy trong Redis, báo lỗi
        if (allProductIds == null) {
            throw new NotFoundException("Session expired or image hash not found.");
        }

        // Áp dụng phân trang
        int startIdx = (request.getPage() - 1) * request.getPageSize();
        int endIdx = Math.min(startIdx + request.getPageSize(), allProductIds.size());

        List<String> paginatedProductIds = allProductIds.subList(startIdx, endIdx);

        // Truy vấn Elasticsearch để lấy thông tin sản phẩm
        List<ProductDocument> products = searchService.findProductsByIds(paginatedProductIds);

        // Metadata response
        Map<String, Object> metadata = Map.of(
                "currentPage", request.getPage(),
                "totalItems", allProductIds.size(),
                "pageSize", request.getPageSize(),
                "totalPages", (int) Math.ceil((double) allProductIds.size() / request.getPageSize())
        );

        return new ImageSearchResponse(request.getImageHash(), products, metadata);
    }
}
