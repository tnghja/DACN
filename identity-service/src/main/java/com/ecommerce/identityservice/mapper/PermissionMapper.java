package com.ecommerce.identityservice.mapper;

import org.mapstruct.Mapper;

import com.ecommerce.identityservice.dto.request.PermissionRequest;
import com.ecommerce.identityservice.dto.response.PermissionResponse;
import com.ecommerce.identityservice.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
