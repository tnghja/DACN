package com.ecommerce.product.controller;

import com.ecommerce.product.model.dto.FileUploadRequest;
import com.ecommerce.product.model.dto.ProductDTO;
import com.ecommerce.product.model.request.ProductCreateRequest;
import com.ecommerce.product.model.request.ProductUpdateRequest;
import com.ecommerce.product.model.response.ApiResponse;
import com.ecommerce.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> productPage = productService.getAllProducts(pageable);

        ApiResponse<List<ProductDTO>> response = new ApiResponse<>();
        response.ok(productPage.getContent());
        response.setMetadata(Map.of(
                "currentPage", productPage.getNumber(),
                "totalItems", productPage.getTotalElements(),
                "totalPages", productPage.getTotalPages(),
                "pageSize", productPage.getSize()
        ));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        ApiResponse<ProductDTO> response = new ApiResponse<>();
        response.ok(productService.getProductById(id).orElse(null));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@RequestBody ProductCreateRequest productCreateRequest) {
        ApiResponse<ProductDTO> response = new ApiResponse<>();
        response.ok(productService.createProduct(productCreateRequest));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateRequest productUpdateRequest) {
        ApiResponse<ProductDTO> response = new ApiResponse<>();
        response.ok(productService.updateProduct(id, productUpdateRequest).orElse(null));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
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
            @PathVariable Long productId,
            @ModelAttribute FileUploadRequest fileUploadRequest) {
        List<MultipartFile> files = fileUploadRequest.getFiles();
        productService.uploadImagesAndAssignToProductAsync(productId, files);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.accepted().body(response);
    }
}
