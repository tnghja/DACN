package com.ecommerce.search_service.model.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String name;
    private String brand;
    private String cover;
    private String description;
    private Double price;
    private Integer quantity; // Số lượng sản phẩm
    private Double rate; // Đánh giá trung bình
    private LocalDateTime deleteAt;



    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
