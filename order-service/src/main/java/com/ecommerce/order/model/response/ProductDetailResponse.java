package com.ecommerce.order.model.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductDetailResponse {
    private Long id;
    private String name;
    private String brand;
    private String cover;
    private String description;
    private Double price;
    private String subCategory;
//    private List<ReviewResponse> reviews;
//
//    @Data
//    @Builder
//    public static class ReviewResponse {
//        private Long id;
//        private String content;
//        private Double rating;
//        private String userName;
//    }
}
