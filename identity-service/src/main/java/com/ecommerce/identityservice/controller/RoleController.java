package com.ecommerce.identityservice.controller;

import java.util.List;


import com.ecommerce.identityservice.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.identityservice.dto.request.RoleRequest;
import com.ecommerce.identityservice.dto.response.RoleResponse;
import com.ecommerce.identityservice.service.RoleService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleController {
    RoleService roleService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> create(@RequestBody RoleRequest request) {
        RoleResponse createdRole = roleService.create(request);
        ApiResponse<RoleResponse> response = new ApiResponse<>();
        response.ok(createdRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAll() {
        List<RoleResponse> roles = roleService.getAll();
        ApiResponse<List<RoleResponse>> response = new ApiResponse<>();
        response.ok(roles);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{role}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String role) {
        roleService.delete(role);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }
}