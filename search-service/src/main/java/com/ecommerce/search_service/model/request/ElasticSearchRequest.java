package com.ecommerce.search_service.model.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ElasticSearchRequest {
    private String name;
    private Long categoryId;

    @Min(value = 0, message = "minPrice must be greater than or equal to 0")
    private Double minPrice;

    @Min(value = 0, message = "maxPrice must be greater than or equal to 0")
    private Double maxPrice;

    @Min(value = 0, message = "minRate must be greater than or equal to 0")
    private Double minRate;

    @Min(value = 0, message = "maxRate must be greater than or equal to 0")
    private Double maxRate;

    @Min(value = 1, message = "page must be greater than or equal to 1")  // Page bắt đầu từ 1
    private int page = 1;

    @Min(value = 1, message = "size must be greater than or equal to 1")
    private int size = 10;

    private String sort;
}