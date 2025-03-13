package com.ecommerce.product.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RatingCrudResponse {
    String message;
    Double newAvgRating;
}
