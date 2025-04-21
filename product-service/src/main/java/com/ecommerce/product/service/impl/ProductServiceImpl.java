package com.ecommerce.product.service.impl;

import com.ecommerce.product.exception.InvalidFileTypeException;
import com.ecommerce.product.exception.NotFoundException;
import com.ecommerce.product.model.dto.ProductDTO;
import com.ecommerce.product.model.entity.Category;
import com.ecommerce.product.model.entity.Image;
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
import java.util.ArrayList;
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

        // Validate category existence
        Category category = categoryRepository.findById(productCreateRequest.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found with ID: " + productCreateRequest.getCategoryId()));

        product.setCategory(category); // Associate the category

        Product createdProduct = productRepository.save(product);
        return productMapper.toDTO(createdProduct);
    }

    @Override
    public Optional<ProductDTO> updateProduct(String id, ProductUpdateRequest productUpdateRequest) {
        return productRepository.findById(id).map(existingProduct -> {
            // Validate product is not deleted
            if (existingProduct.getDeleteAt() != null) {
                throw new IllegalStateException("Cannot update a deleted product");
            }

            // PATCH-style update: only update fields present in ProductUpdateRequest
            if (productUpdateRequest.getName() != null) {
                existingProduct.setName(productUpdateRequest.getName());
            }
            if (productUpdateRequest.getBrand() != null) {
                existingProduct.setBrand(productUpdateRequest.getBrand());
            }
            if (productUpdateRequest.getCover() != null) {
                existingProduct.setCover(productUpdateRequest.getCover());
            }
            if (productUpdateRequest.getDescription() != null) {
                existingProduct.setDescription(productUpdateRequest.getDescription());
            }
            if (productUpdateRequest.getPrice() != null) {
                existingProduct.setPrice(productUpdateRequest.getPrice());
            }
            if (productUpdateRequest.getQuantity() != null) {
                existingProduct.setQuantity(productUpdateRequest.getQuantity());
            }
            if (productUpdateRequest.getCategoryId() != null) {
                Category category = categoryRepository.findById(productUpdateRequest.getCategoryId())
                        .orElseThrow(() -> new NotFoundException("Category not found with ID: " + productUpdateRequest.getCategoryId()));
                existingProduct.setCategory(category);
            }
            // Images, ratings, and rate are only updated by their own endpoints/processes
            // (If you want to support patching images here, add logic for that)

            Product savedProduct = productRepository.save(existingProduct);
            return productMapper.toDTO(savedProduct);
        });
    }

    @Override
    public boolean deleteProduct(String id) {
        return productRepository.findById(id).map(product -> {
            // Check if product is already deleted
            if (product.getDeleteAt() != null) {
                return false; // Product already deleted
            }
            
            // Cleanup associated Cloudinary resources if images exist
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                for (Image image : product.getImages()) {
                    try {
                        String imageUrl = image.getUrl();
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            // Extract public ID from the URL - this will depend on your URL structure
                            // For example, if your URL is https://res.cloudinary.com/cloud-name/image/upload/v123456/my-image-id.jpg
                            // the public ID would be my-image-id
                            String publicId = getPublicIdFromUrl(imageUrl);
                            cloudinaryService.deleteFile(publicId);
                        }
                    } catch (Exception e) {
                        // Log error but continue with other images
                        System.err.println("Failed to delete image: " + image.getUrl() + " - " + e.getMessage());
                    }
                }
            }
            
            // Mark the product as deleted by setting `deleteAt` timestamp
            product.setDeleteAt(java.time.LocalDateTime.now());
            productRepository.save(product);
            
            return true;
        }).orElse(false);
    }

    private String getPublicIdFromUrl(String url) {
        // Simple method to extract the public ID from a Cloudinary URL
        // Modify this based on your actual URL structure
        try {
            if (url == null) return null;
            
            // Example implementation for URL like: https://res.cloudinary.com/cloud-name/image/upload/v123456/public-id.jpg
            String[] parts = url.split("/");
            if (parts.length > 0) {
                String lastPart = parts[parts.length - 1];
                // Remove file extension if present
                int dotIndex = lastPart.lastIndexOf('.');
                if (dotIndex > 0) {
                    return lastPart.substring(0, dotIndex);
                }
                return lastPart;
            }
        } catch (Exception e) {
            System.err.println("Error extracting public ID: " + e.getMessage());
        }
        return null;
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
        // Fetch the product by ID
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Create a new Image object and set its URL
        Image image = new Image();
        image.setUrl(imageUrl);

        // Add the new image to the product's image list
        if (product.getImages() == null) {
            product.setImages(new ArrayList<>()); // Initialize the list if it's null
        }
        product.getImages().add(image);

        // Save the updated product (this will cascade and save the new image)
        productRepository.save(product);
    }



}
