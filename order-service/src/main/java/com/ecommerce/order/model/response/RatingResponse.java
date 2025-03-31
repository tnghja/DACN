package com.ecommerce.order.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RatingResponse {
    String message;
    Integer rating;
}
