package com.ecommerce.order.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductCreateRequest {
    @NotNull
    private String name;
    @NotNull
    private String brand;
    @NotNull
    private String cover;
    @NotNull
    private String description;
    @NotNull
    private Double price;
    @NotNull
    private Integer quantity;
    private Double rate;
    @NotNull
    private Long categoryId;
}
