package com.ecommerce.inventory.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@Table(name = "inventory_product")
public class Product {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;
    @PrePersist
    public void generateId() {
        this.id = "S" + UUID.randomUUID().toString().replace("-", "").substring(0, 11);
    }
    private String name;
    private String brand;
    private String cover;
    private String description;
    private Double price;
    private Integer quantity; // Số lượng sản phẩm
    private Double rate; // Đánh giá trung bình
    private LocalDateTime deleteAt;

//    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
//    @JoinColumn(name = "product_id", nullable = false)
//    private List<Image> images; // Danh sách đường dẫn ảnh
//    @JsonIgnore // Bỏ qua trường này khi serialize
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Rating> ratings;

//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<CouponProduct> couponProducts;

    private Long categoryId;
}
