package com.ecommerce.product.service;

import com.ecommerce.product.model.dto.ProductDTO;
import com.ecommerce.product.model.request.ImageUploadRequest;
import com.ecommerce.product.model.request.ProductCreateRequest;
import com.ecommerce.product.model.request.ProductUpdateRequest;
import com.ecommerce.product.model.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {
   Page<ProductDTO> getAllProducts(Pageable pageable);
   
   ApiResponse<List<ProductDTO>> getPaginatedProducts(int page, int size);

   Optional<ProductDTO> getProductById(String id);

   Optional<ProductDTO> updateProduct(String id, ProductUpdateRequest productUpdateRequest);

   ProductDTO createProduct(ProductCreateRequest productDTO);

   boolean deleteProduct(String id);

   void uploadImagesAndAssignToProductAsync(String productId, List<ImageUploadRequest> files);
}
