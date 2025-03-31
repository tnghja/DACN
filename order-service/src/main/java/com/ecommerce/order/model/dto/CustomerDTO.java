package com.ecommerce.order.model.dto;


import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    @Column(nullable = false)
    private String userId;
    @Column(nullable = false)
    private String userName;
}
