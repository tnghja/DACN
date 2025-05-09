package com.ecommerce.product.service;

import com.ecommerce.product.model.dto.CategoryDTO;
import com.ecommerce.product.model.entity.Category;
import com.ecommerce.product.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public interface CategoryService {

    List<CategoryDTO> getAllCategories();

    Page<CategoryDTO> getAllCategories(Pageable pageable);

    CategoryDTO getCategoryById(Long id);

    CategoryDTO createCategory(CategoryDTO categoryDTO);

    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);

    void softDeleteCategory(Long id);

    void restoreCategory(Long id);

    List<CategoryDTO> assignParentToCategories(Long parentId, List<Long> categoryIds);

    List<CategoryDTO> getSubcategories(Long parentId);
}
