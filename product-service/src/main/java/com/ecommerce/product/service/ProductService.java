package com.ecommerce.product.service;

import com.ecommerce.product.model.dto.ProductDTO;
import com.ecommerce.product.model.request.ProductCreateRequest;
import com.ecommerce.product.model.request.ProductUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ProductService {
   Page<ProductDTO> getAllProducts(Pageable pageable);

    Optional<ProductDTO> getProductById(Long id);


    Optional<ProductDTO> updateProduct(Long id, ProductUpdateRequest productUpdateRequest);

    ProductDTO createProduct(ProductCreateRequest productDTO);

    boolean deleteProduct(Long id);



 void uploadImagesAndAssignToProductAsync(Long productId, List<MultipartFile> files);
}
