package com.ecommerce.product.controller;

import com.ecommerce.product.model.dto.FileUploadRequest;
import com.ecommerce.product.model.dto.ProductDTO;
import com.ecommerce.product.model.request.ImageUploadRequest;
import com.ecommerce.product.model.request.ProductCreateRequest;
import com.ecommerce.product.model.request.ProductUpdateRequest;
import com.ecommerce.product.model.response.ApiResponse;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        ApiResponse<List<ProductDTO>> response = productService.getPaginatedProducts(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable String id) {
        ApiResponse<ProductDTO> response = new ApiResponse<>();
        response.ok(productService.getProductById(id).orElse(null));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductCreateRequest productCreateRequest) {
        ApiResponse<ProductDTO> response = new ApiResponse<>();
        response.ok(productService.createProduct(productCreateRequest));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(@PathVariable String id, @Valid @RequestBody ProductUpdateRequest productUpdateRequest) {
        ApiResponse<ProductDTO> response = new ApiResponse<>();
        response.ok(productService.updateProduct(id, productUpdateRequest).orElse(null));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        ApiResponse<Void> response = new ApiResponse<>();

        try {
            boolean deleted = productService.deleteProduct(id);
            if (deleted) {
                response.ok();
                return ResponseEntity.ok(response);
            } else {
                response.error(Map.of("message", "Product not found"));
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception ex) {
            response.error(Map.of("message", "An error occurred: " + ex.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/{productId}/upload-images")
    public ResponseEntity<ApiResponse<Void>> uploadImagesToProduct(
            @PathVariable String productId,
            @ModelAttribute FileUploadRequest fileUploadRequest) {
        List<MultipartFile> files = fileUploadRequest.getFiles();

        // Convert files to byte arrays before passing to async method
        List<ImageUploadRequest> imageUploadRequests = files.stream().map(file -> {
            try {
                return new ImageUploadRequest(file.getOriginalFilename(), file.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Error reading file: " + file.getOriginalFilename(), e);
            }
        }).toList();

        productService.uploadImagesAndAssignToProductAsync(productId, imageUploadRequests);

        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.accepted().body(response);
    }

}
