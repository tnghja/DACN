package com.ecommerce.product.service;

import com.ecommerce.product.model.request.RatingRequest;
import com.ecommerce.product.model.response.RatingCrudResponse;
import com.ecommerce.product.model.response.RatingResponse;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface RatingService {
    @Transactional
    RatingCrudResponse createRating(RatingRequest ratingRequest);

    @Transactional
    RatingCrudResponse updateRating(RatingRequest ratingRequest);

    RatingResponse getRatingByUserIdAndProductId(String userId, String productId);

    Page<RatingResponse> getRatingsByProductId(String productId, int page, int size);
}
