package com.ecommerce.product.service.impl;

import com.ecommerce.product.exception.InvalidFileTypeException;
import com.ecommerce.product.exception.NotFoundException;
import com.ecommerce.product.model.dto.ProductDTO;
import com.ecommerce.product.model.entity.Category;
import com.ecommerce.product.model.entity.Product;
import com.ecommerce.product.model.mapper.ProductMapper;
import com.ecommerce.product.model.request.ImageUploadRequest;
import com.ecommerce.product.model.request.ProductCreateRequest;
import com.ecommerce.product.model.request.ProductUpdateRequest;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.CloudinaryService;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.product.util.FileAsyncUtil;
import com.ecommerce.product.validation.FileValidation;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private FileAsyncUtil fileAsyncUtil;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Override
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::toDTO);
    }

    @Override
    public Optional<ProductDTO> getProductById(String id) {
        return productRepository.findById(id).map(productMapper::toDTO);
    }

    @Override
    public ProductDTO createProduct(ProductCreateRequest productCreateRequest) {

            Product product = productMapper.toEntity(productCreateRequest);

            Category category = categoryRepository.findById(productCreateRequest.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found"));

            product.setCategory(category); // Associate the category

            Product createdProduct = productRepository.save(product);
            return productMapper.toDTO(createdProduct);


    }

    @Override
    public Optional<ProductDTO> updateProduct(String id, ProductUpdateRequest productUpdateRequest) {
        return productRepository.findById(id).map(existingProduct -> {
            Product updatedProduct = productMapper.toEntity(productUpdateRequest);

            // Preserve the existing ID, reviews, and deleteAt fields
            updatedProduct.setId(existingProduct.getId());
//            updatedProduct.setReviews(existingProduct.getReviews());
            updatedProduct.setDeleteAt(existingProduct.getDeleteAt());

            Product savedProduct = productRepository.save(updatedProduct);
            return productMapper.toDTO(savedProduct);
        });
    }

    @Override
    public boolean deleteProduct(String id) {
        return productRepository.findById(id).map(product -> {
            // Mark the product as deleted by setting `deleteAt` timestamp
            product.setDeleteAt(java.time.LocalDateTime.now());
            productRepository.save(product);
            return true;
        }).orElse(false);
    }

    @Async
    public void uploadImagesAndAssignToProductAsync(String productId, List<ImageUploadRequest> imageRequests) {
        imageRequests.forEach(image -> CompletableFuture.runAsync(() -> {
            try {
                String imageUrl = cloudinaryService.uploadImage(image.getFileData(), image.getFileName()); // Upload image
                updateProductWithImage(productId, imageUrl); // Update product with image URL
            } catch (IOException | InvalidFileTypeException e) {
                System.err.println("Error uploading image for productId " + productId + ": " + e.getMessage());
            }
        }));
    }


    private void updateProductWithImage(String productId, String imageUrl) {

        Product product = productRepository.findByProductId(productId);

        product.getImages().add(imageUrl);
        productRepository.save(product); // Save updated product


    }



}
