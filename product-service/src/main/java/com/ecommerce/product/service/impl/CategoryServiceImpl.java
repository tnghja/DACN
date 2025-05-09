package com.ecommerce.product.service.impl;

import com.ecommerce.product.exception.BadRequestException;
import com.ecommerce.product.exception.NotFoundException;
import com.ecommerce.product.model.dto.CategoryDTO;
import com.ecommerce.product.model.entity.Category;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<CategoryDTO> getAllCategories() {
        return convertToDTOsWithSubcategories(categoryRepository.findAllActive());
    }
    
    @Override
    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        return categoryRepository.findAllActive(pageable)
                .map(this::convertToDTOWithSubcategories);
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
        return convertToDTOWithSubcategories(category);
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category parentCategory = null;
        if (categoryDTO.getParentId() != null) {
            parentCategory = categoryRepository.findActiveById(categoryDTO.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent category not found with id: " + categoryDTO.getParentId()));
        }
        
        Category category = Category.builder()
                .name(categoryDTO.getName())
                .parentCategory(parentCategory)
                .build();
        
        category = categoryRepository.save(category);
        return convertToDTOWithSubcategories(category);
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));

        if (categoryDTO.getName() != null) {
            category.setName(categoryDTO.getName());
        }

        if (categoryDTO.getParentId() != null) {
            // Validate to prevent circular references
            if (categoryDTO.getParentId().equals(id)) {
                throw new BadRequestException("A category cannot be its own parent");
            }
            
            Category parentCategory = categoryRepository.findActiveById(categoryDTO.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent category not found with id: " + categoryDTO.getParentId()));
            
            // Check for circular reference
            if (isCircularReference(parentCategory, id)) {
                throw new BadRequestException("Cannot create circular reference in category hierarchy");
            }
            
            category.setParentCategory(parentCategory);
        }

        category = categoryRepository.save(category);
        return convertToDTOWithSubcategories(category);
    }

    /**
     * Checks if assigning a parent category would create a circular reference
     * @param parentCategory the potential parent category
     * @param childId the ID of the child category
     * @return true if there would be a circular reference, false otherwise
     */
    private boolean isCircularReference(Category parentCategory, Long childId) {
        // If no parent, no circular reference possible
        if (parentCategory == null) {
            return false;
        }
        
        // Start with the potential parent
        Category current = parentCategory;
        Set<Long> visitedIds = new HashSet<>();
        
        // Walk up the hierarchy
        while (current != null) {
            // If we've seen this category before or it equals the child, we have a circular reference
            if (!visitedIds.add(current.getId()) || current.getId().equals(childId)) {
                return true;
            }
            
            // Move up to the parent
            current = current.getParentCategory();
        }
        
        return false;
    }

    @Override
    @Transactional
    public void softDeleteCategory(Long id) {
        Category category = categoryRepository.findActiveById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
        
        // Soft delete the category
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }
    
    @Override
    @Transactional
    public void restoreCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + id));
        
        if (category.getDeletedAt() == null) {
            throw new BadRequestException("Category is not deleted");
        }
        
        // Check if parent is deleted
        if (category.getParentCategory() != null && category.getParentCategory().getDeletedAt() != null) {
            throw new BadRequestException("Cannot restore category with deleted parent");
        }
        
        // Restore the category
        category.setDeletedAt(null);
        categoryRepository.save(category);
    }
    
    @Override
    @Transactional
    public List<CategoryDTO> assignParentToCategories(Long parentId, List<Long> categoryIds) {
        Category parentCategory;

        // Validate parent category
        if (parentId != null) {
            parentCategory = categoryRepository.findActiveById(parentId)
                    .orElseThrow(() -> new NotFoundException("Parent category not found with id: " + parentId));
        } else {
            parentCategory = null;
        }

        // Check for circular references
        if (parentId != null) {
            for (Long categoryId : categoryIds) {
                if (categoryId.equals(parentId)) {
                    throw new BadRequestException("A category cannot be its own parent");
                }
                
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
                
                if (isCircularReference(parentCategory, categoryId)) {
                    throw new BadRequestException("Cannot create circular reference in category hierarchy");
                }
            }
        }

        // Assign parent category to each category in the list
        List<Category> updatedCategories = categoryRepository.findAllById(categoryIds).stream()
                .peek(category -> {
                    if (category == null || category.getDeletedAt() != null) {
                        throw new NotFoundException("Category not found or is deleted");
                    }
                    category.setParentCategory(parentCategory);
                })
                .collect(Collectors.toList());

        // Save the updated categories
        List<Category> savedCategories = categoryRepository.saveAll(updatedCategories);

        // Return the DTOs of the updated categories
        return convertToDTOsWithSubcategories(savedCategories);
    }
    
    @Override
    public List<CategoryDTO> getSubcategories(Long parentId) {
        List<Category> subcategories = categoryRepository.findActiveByParentId(parentId);
        return convertToDTOsWithSubcategories(subcategories);
    }
    
    // Helper method to convert a Category to CategoryDTO with subcategories
    private CategoryDTO convertToDTOWithSubcategories(Category category) {
        if (category == null) {
            return null;
        }
        
        List<CategoryDTO> subcategoryDTOs = category.getSubCategories().stream()
                .filter(subCategory -> subCategory.getDeletedAt() == null)
                .map(subCategory -> CategoryDTO.builder()
                        .id(subCategory.getId())
                        .name(subCategory.getName())
                        .parentId(category.getId())
                        .build())
                .collect(Collectors.toList());
        
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .subCategories(subcategoryDTOs)
                .build();
    }
    
    // Helper method to convert a list of Categories to CategoryDTOs with subcategories
    private List<CategoryDTO> convertToDTOsWithSubcategories(List<Category> categories) {
        if (categories == null) {
            return new ArrayList<>();
        }
        
        return categories.stream()
                .map(this::convertToDTOWithSubcategories)
                .collect(Collectors.toList());
    }
}
