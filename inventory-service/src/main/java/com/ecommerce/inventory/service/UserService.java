//package com.ecommerce.inventory.service;
//
//import com.ecommerce.inventory.model.entity.Address;
//import com.ecommerce.inventory.model.request.AddressCreateRequest;
//import com.ecommerce.inventory.model.request.UpdateUserProfileRequest;
//
//import com.ecommerce.inventory.model.response.UpdateUserProfileResponse;
//import com.ecommerce.inventory.model.response.UserProfileCreateResponse;
//import com.ecommerce.inventory.model.response.UserInfoResponse;
//import com.ecommerce.inventory.model.request.UserProfileCreateRequest;
//
//import java.util.List;
//
//public interface UserService {
//    UserProfileCreateResponse createUserProfile(UserProfileCreateRequest userProfileCreateRequest);
//    UpdateUserProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest request);
//    Address addAddressToUser(Long userId, AddressCreateRequest addressCreateRequest);
//
//    List<Address> getAddress(Long userId);
//
//    UserInfoResponse getUser(Long userId);
//}
