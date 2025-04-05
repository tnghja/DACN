package com.ecommerce.search_service.service.impl;

import com.ecommerce.search_service.exception.ImageProcessingException;
import com.ecommerce.search_service.service.ImageVectorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ImageVectorServiceImpl implements ImageVectorService {

    private final WebClient webClient;
    private final String imageProcessingUrl;

    public ImageVectorServiceImpl(WebClient webClient,
                                @Value("${image.processing.url}") String imageProcessingUrl) {
        this.webClient = webClient;
        this.imageProcessingUrl = imageProcessingUrl;
    }

    @Override
    public List<Float> extractImageVector(MultipartFile file) {
        try {
            Map<String, Object> response = webClient.post()
                    .uri(imageProcessingUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData("file", file.getResource()))
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response1 -> Mono.error(new ImageProcessingException("Error processing image: " + response1.statusCode())))
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null || !response.containsKey("vector")) {
                throw new ImageProcessingException("No vector returned from image processing service");
            }

            @SuppressWarnings("unchecked")
            List<Double> vector = (List<Double>) response.get("vector");
            return vector.stream()
                    .map(Double::floatValue)
                    .toList();
        } catch (Exception e) {
            throw new ImageProcessingException("Error processing image: " + e.getMessage());
        }
    }
} 