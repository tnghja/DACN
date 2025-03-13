package com.ecommerce.user.model.mapper;

import com.ecommerce.user.model.entity.User;
import com.ecommerce.user.model.request.UserCreateRequest;
import com.ecommerce.user.model.request.UserProfileCreateRequest;
import com.ecommerce.user.model.response.UpdateUserProfileResponse;

import com.ecommerce.user.model.response.UserInfoResponse;
import com.ecommerce.user.model.response.UserProfileCreateResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public abstract class UserMapper {
    public abstract User toEntity(UserProfileCreateRequest userCreateRequest);
    public abstract UserProfileCreateResponse toUserCreateResponse(User user);
    public abstract UpdateUserProfileResponse toUpdateUserProfileResponse(User user);
    public abstract UserInfoResponse toUserInfoResponse(User user);
}
