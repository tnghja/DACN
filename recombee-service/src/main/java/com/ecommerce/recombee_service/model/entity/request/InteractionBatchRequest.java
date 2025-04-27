package com.ecommerce.recombee_service.model.entity.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;
@Getter
@Data
@AllArgsConstructor
public class InteractionBatchRequest {
    @NotBlank(message = "userId is required")
    @JsonProperty("userId")
    private String userId;
    @Size(min = 1, message = "itemIds is required")
    @JsonProperty("itemIds")
    private List<String> itemIds;
}
