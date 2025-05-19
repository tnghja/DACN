package com.ecommerce.recombee_service.model.entity.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InteractionBatchRequest {
    @NotNull(message = "userId is required")
    @NotBlank(message = "userId is required")
    private String userId;
    @Size(min = 1, message = "itemIds are required")
    private List<String> itemIds;
}
