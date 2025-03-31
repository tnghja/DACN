package com.ecommerce.order.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RatingCrudResponse {
    String message;
    Double newAvgRating;
}
