package com.ecommerce.recombee_service.model.entity.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InteractionRequest {
    @NotBlank(message = "userId is required")
    @JsonProperty("userId")
    private String userId;

    @NotBlank(message = "itemId is required")
    @JsonProperty("itemId")
    private String itemId;
}