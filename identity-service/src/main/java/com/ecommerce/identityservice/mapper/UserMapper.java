package com.ecommerce.identityservice.mapper;

import com.ecommerce.identityservice.dto.request.UserCreationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.ecommerce.identityservice.dto.request.UserUpdateRequest;
import com.ecommerce.identityservice.dto.response.UserResponse;
import com.ecommerce.identityservice.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
