package com.ecommerce.product.repository;

import com.ecommerce.product.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    @Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL")
    List<Category> findAllActive();
    
    @Query("SELECT c FROM Category c WHERE c.deletedAt IS NULL")
    Page<Category> findAllActive(Pageable pageable);
    
    @Query("SELECT c FROM Category c WHERE c.id = :id AND c.deletedAt IS NULL")
    Optional<Category> findActiveById(@Param("id") Long id);
    
    @Query("SELECT c FROM Category c WHERE c.parentCategory.id = :parentId AND c.deletedAt IS NULL")
    List<Category> findActiveByParentId(@Param("parentId") Long parentId);
}
