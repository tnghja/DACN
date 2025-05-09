package com.ecommerce.product.controller;

import com.ecommerce.product.model.dto.CategoryDTO;
import com.ecommerce.product.model.request.AssignParentRequest;
import com.ecommerce.product.model.response.ApiResponse;
import com.ecommerce.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : 
                Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CategoryDTO> categoryPage = categoryService.getAllCategories(pageable);

        ApiResponse<List<CategoryDTO>> response = new ApiResponse<>();
        response.ok(categoryPage.getContent());
        response.setMetadata(Map.of(
                "currentPage", categoryPage.getNumber(),
                "totalItems", categoryPage.getTotalElements(),
                "totalPages", categoryPage.getTotalPages(),
                "pageSize", categoryPage.getSize()
        ));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        ApiResponse<CategoryDTO> response = new ApiResponse<>();
        response.ok(categoryService.getCategoryById(id));
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/subcategories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getSubcategories(@PathVariable Long id) {
        ApiResponse<List<CategoryDTO>> response = new ApiResponse<>();
        response.ok(categoryService.getSubcategories(id));
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@RequestBody CategoryDTO categoryDTO) {
        ApiResponse<CategoryDTO> response = new ApiResponse<>();
        response.ok(categoryService.createCategory(categoryDTO));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        ApiResponse<CategoryDTO> response = new ApiResponse<>();
        response.ok(categoryService.updateCategory(id, categoryDTO));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.softDeleteCategory(id);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> restoreCategory(@PathVariable Long id) {
        categoryService.restoreCategory(id);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/assign-parent")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> assignParentToCategories(@RequestBody AssignParentRequest request) {
        ApiResponse<List<CategoryDTO>> response = new ApiResponse<>();
        response.ok(categoryService.assignParentToCategories(request.getParentId(), request.getCategoryIds()));
        return ResponseEntity.ok(response);
    }
}