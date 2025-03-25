package com.ecommerce.product.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Table(name = "product_images")
public class Image {
    @Id
    @Column(nullable = false, updatable = false)
    private String id;


    @PrePersist
    public void generateId() {
        this.id = "I" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
    }
    @Column(nullable = false)
    private String url;
}
