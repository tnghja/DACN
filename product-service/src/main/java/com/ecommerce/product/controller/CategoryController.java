package com.ecommerce.product.controller;

import com.ecommerce.product.model.dto.CategoryDTO;
import com.ecommerce.product.model.request.AssignParentRequest;
import com.ecommerce.product.model.response.ApiResponse;
import com.ecommerce.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        ApiResponse<List<CategoryDTO>> response = new ApiResponse<>();
        response.ok(categoryService.getAllCategories());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        ApiResponse<CategoryDTO> response = new ApiResponse<>();
        response.ok(categoryService.getCategoryById(id));
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
        categoryService.deleteCategory(id);
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