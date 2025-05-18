package com.ecommerce.recombee_service.model.entity.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContextRequest {
    @JsonProperty("recentlyViewedItems")
    private List<String> recentlyViewedItems;
    
    @JsonProperty("categories")
    private List<String> categories;
} 