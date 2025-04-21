package com.ecommerce.product.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingRequest {
    @NotNull
    private String userId;

    @NotNull
    private String productId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;
}

