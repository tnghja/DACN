package com.ecommerce.product.service.impl;

import com.ecommerce.product.model.entity.*;

import com.ecommerce.product.model.request.RatingRequest;
import com.ecommerce.product.model.response.RatingCrudResponse;
import com.ecommerce.product.model.response.RatingResponse;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.repository.RatingRepository;

import com.ecommerce.product.repository.OrderRepository;

import com.ecommerce.product.repository.UserRepository;
import com.ecommerce.product.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements  RatingService {

    private final ProductRepository productRepository;
    private final RatingRepository ratingRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RatingCrudResponse createRating(RatingRequest ratingRequest) {
        Product product = productRepository.findById(ratingRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        boolean hasApprovedOrder = orderRepository.existsByCustomerIdAndProductIdAndStatus(
                ratingRequest.getUserId(), ratingRequest.getProductId(), PaymentStatus.APPROVED);

        User user = userRepository.findById(ratingRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!(user instanceof Customer customer)) {
            throw new RuntimeException("User must be a customer to leave a review");
        }
        if (!hasApprovedOrder) {
            throw new RuntimeException("Cannot rate this product. Order must be approved first.");
        }

        Rating rating = Rating.builder()
                .content(ratingRequest.getComment())
                .rate(ratingRequest.getRating())
                .createAt(LocalDateTime.now())
                .customer(customer)
                .product(product)
                .build();

        ratingRepository.save(rating);
        updateProductRating(product);

        return new RatingCrudResponse("Rating submitted successfully", product.getRate());
    }

    @Override
    @Transactional
    public RatingCrudResponse updateRating(RatingRequest ratingRequest) {
        Rating rating = ratingRepository.findByCustomerIdAndProductId(
                        ratingRequest.getUserId(), ratingRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Review not found"));

        rating.setContent(ratingRequest.getComment());
        rating.setRate(ratingRequest.getRating());
        rating.setCreateAt(LocalDateTime.now());

        ratingRepository.save(rating);
        updateProductRating(rating.getProduct());

        return new RatingCrudResponse("Rating submitted successfully",rating.getProduct().getRate());
    }


    @Override
    public RatingResponse getRatingByUserIdAndProductId(Long userId, String productId) {
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
