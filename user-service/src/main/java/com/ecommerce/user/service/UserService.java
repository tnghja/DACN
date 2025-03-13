package com.ecommerce.user.service;

import com.ecommerce.user.model.entity.Address;
import com.ecommerce.user.model.request.AddressCreateRequest;
import com.ecommerce.user.model.request.UpdateUserProfileRequest;

import com.ecommerce.user.model.response.UpdateUserProfileResponse;
import com.ecommerce.user.model.response.UserProfileCreateResponse;
import com.ecommerce.user.model.response.UserInfoResponse;
import com.ecommerce.user.model.request.UserProfileCreateRequest;

import java.util.List;

public interface UserService {
    UserProfileCreateResponse createUserProfile(UserProfileCreateRequest userProfileCreateRequest);
    UpdateUserProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest request);
    Address addAddressToUser(Long userId, AddressCreateRequest addressCreateRequest);

    List<Address> getAddress(Long userId);

    UserInfoResponse getUser(Long userId);
}
