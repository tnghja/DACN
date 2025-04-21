package com.ecommerce.inventory.repository;


import com.ecommerce.inventory.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    // You can add custom query methods if needed, e.g.:
    // List<Product> findByIdIn(List<String> ids);
}