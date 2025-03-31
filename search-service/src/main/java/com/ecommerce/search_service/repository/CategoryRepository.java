package com.ecommerce.search_service.repository;

import com.ecommerce.search_service.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}