package com.ecommerce.product.service.impl;

import com.ecommerce.product.model.entity.*;

import com.ecommerce.product.model.request.RatingRequest;
import com.ecommerce.product.model.response.RatingCrudResponse;
import com.ecommerce.product.model.response.RatingResponse;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.repository.RatingRepository;

import com.ecommerce.product.repository.httpClient.OrderClient;
import com.ecommerce.product.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements  RatingService {

    private final ProductRepository productRepository;
    private final RatingRepository ratingRepository;
//    private final OrderRepository orderRepository;
//    private final UserRepository userRepository;
    private final OrderClient orderClient;
@Override
@Transactional
public RatingCrudResponse createRating(RatingRequest ratingRequest) {
    Product product = productRepository.findById(ratingRequest.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

    // Ensure the user has an approved order for this product before rating
    ResponseEntity<Boolean> response = orderClient.existsByCustomerIdAndOrderItemsProductIdAndStatus(
            ratingRequest.getUserId(), ratingRequest.getProductId(), PaymentStatus.APPROVED);
    
    // Safely check if response exists and is true
    boolean hasApprovedOrder = response != null && Boolean.TRUE.equals(response.getBody());

    if (!hasApprovedOrder) {
        throw new RuntimeException("Cannot rate this product. You must have an approved order.");
    }

    // Create and save the rating
    Rating rating = Rating.builder()
            .content(ratingRequest.getComment())
            .rate(ratingRequest.getRating())
            .createAt(LocalDateTime.now())
            .customerId(ratingRequest.getUserId())
            .product(product)
            .build();

    ratingRepository.save(rating);

    // Update the product's rating score
    updateProductRating(product);

    return new RatingCrudResponse("Rating submitted successfully", product.getRate());
}


    @Override
    @Transactional
    public RatingCrudResponse updateRating(RatingRequest ratingRequest) {
        Rating rating = ratingRepository.findByCustomerIdAndProductId(
                        ratingRequest.getUserId(), ratingRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Review not found"));
        boolean updated = false;
        if (ratingRequest.getComment() != null) {
            rating.setContent(ratingRequest.getComment());
            updated = true;
        }
        if (ratingRequest.getRating() != null) {
            rating.setRate(ratingRequest.getRating());
            updated = true;
        }
        if (updated) {
            rating.setCreateAt(LocalDateTime.now());
            ratingRepository.save(rating);
            updateProductRating(rating.getProduct());
        }
        return new RatingCrudResponse("200", rating.getProduct().getRate());
    }


    @Override
    public RatingResponse getRatingByUserIdAndProductId(String userId, String productId) {
        Rating rating = ratingRepository.findByCustomerIdAndProductId(userId, productId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return new RatingResponse(rating.getContent(), rating.getRate());
    }

    @Override
    public Page<RatingResponse> getRatingsByProductId(String productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
        Page<Rating> ratingPage = ratingRepository.findByProductId(productId, pageable);

        return ratingPage.map(rating -> new RatingResponse(rating.getContent(), rating.getRate()));
    }
    private void updateProductRating(Product product) {
        List<Rating> ratings = ratingRepository.findByProductId(product.getId());
        Double avgRating = ratings.stream().mapToDouble(Rating::getRate).average().orElse(0);
        product.setRate(avgRating);
        productRepository.save(product);
    }
}
