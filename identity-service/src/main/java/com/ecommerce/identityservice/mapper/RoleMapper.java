package com.ecommerce.identityservice.mapper;

import com.ecommerce.identityservice.dto.response.RoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ecommerce.identityservice.dto.request.RoleRequest;
import com.ecommerce.identityservice.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
