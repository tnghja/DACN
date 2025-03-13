package com.ecommerce.notification_service.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@AllArgsConstructor
@Getter
@Setter
public class ProductResponse {
    private String id;

    private String name;
    private String brand;
    private String cover;
    private String description;
    private Double price;
    private Integer quantity; // Số lượng sản phẩm
    private Double rate;
    private String categoryName;
}
