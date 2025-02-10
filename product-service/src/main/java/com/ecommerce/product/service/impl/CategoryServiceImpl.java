package com.ecommerce.product.service.impl;

import com.ecommerce.product.exception.NotFoundException;
import com.ecommerce.product.model.dto.CategoryDTO;
import com.ecommerce.product.model.entity.Category;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> CategoryDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .parentId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                        .build())
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .build();
    }

    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category parentCategory = null;
        if (categoryDTO.getParentId() != null) {
            parentCategory = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent category not found with id: " + categoryDTO.getParentId()));
        }
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .parentCategory(parentCategory)
                .build();
        category = categoryRepository.save(category);
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(parentCategory != null ? parentCategory.getId() : null)
                .build();
    }

    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
        Category parentCategory = null;
        if (categoryDTO.getParentId() != null) {
            parentCategory = categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent category not found with id: " + categoryDTO.getParentId()));
        }
        category.setName(categoryDTO.getName());
        category.setParentCategory(parentCategory);
        category = categoryRepository.save(category);
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(parentCategory != null ? parentCategory.getId() : null)
                .build();
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
    public List<CategoryDTO> assignParentToCategories(Long parentId, List<Long> categoryIds) {
        Category parentCategory;

        // Kiểm tra parentId có hợp lệ không
        if (parentId != null) {
            parentCategory = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new NotFoundException("Parent category not found with id: " + parentId));
        } else {
            parentCategory = null;
        }

        // Gán parentCategory cho từng Category trong danh sách
        List<Category> updatedCategories = categoryRepository.findAllById(categoryIds).stream()
                .peek(category -> {
                    if (category == null) {
                        throw new NotFoundException("Category not found with given id in list");
                    }
                    category.setParentCategory(parentCategory);
                })
                .collect(Collectors.toList());

        // Lưu danh sách Category đã được cập nhật
        List<Category> savedCategories = categoryRepository.saveAll(updatedCategories);

        // Trả về danh sách DTO của các danh mục đã được cập nhật
        return savedCategories.stream()
                .map(category -> CategoryDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .parentId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                        .build())
                .collect(Collectors.toList());
    }

}
