package com.ecommerce.search_service.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ProductListRequest {
    @NotNull(message = "Product IDs list must not be null")
    @NotEmpty(message = "Product IDs list must not be empty")
    @Size(min = 1, max = 100, message = "Product IDs list must contain between 1 and 100 elements")
    private List<String> productIds;
}
