package com.ecommerce.product.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@EntityListeners(com.ecommerce.product.listener.ProductEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "product_id", nullable = false)
    private List<Image> images; // Danh sách đường dẫn ảnh
    @JsonIgnore // Bỏ qua trường này khi serialize
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings;

//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<CouponProduct> couponProducts;
    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
