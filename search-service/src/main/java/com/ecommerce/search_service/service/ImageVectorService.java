package com.ecommerce.search_service.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageVectorService {
    List<Float> extractImageVector(MultipartFile file);
} 