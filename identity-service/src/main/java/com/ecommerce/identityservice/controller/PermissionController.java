package com.ecommerce.identityservice.controller;

import java.util.List;


import com.ecommerce.identityservice.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.identityservice.dto.request.PermissionRequest;
import com.ecommerce.identityservice.dto.response.PermissionResponse;
import com.ecommerce.identityservice.service.PermissionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PermissionController {
    PermissionService permissionService;

    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> create(@RequestBody PermissionRequest request) {
        PermissionResponse createdPermission = permissionService.create(request);
        ApiResponse<PermissionResponse> response = new ApiResponse<>();
        response.ok(createdPermission);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAll() {
        List<PermissionResponse> permissions = permissionService.getAll();
        ApiResponse<List<PermissionResponse>> response = new ApiResponse<>();
        response.ok(permissions);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{permission}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String permission) {
        permissionService.delete(permission);
        ApiResponse<Void> response = new ApiResponse<>();
        response.ok();
        return ResponseEntity.ok(response);
    }
}
