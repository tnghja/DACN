package com.ecommerce.user.controller;

import com.ecommerce.user.model.entity.Address;
import com.ecommerce.user.model.entity.User;
import com.ecommerce.user.model.request.AddressCreateRequest;
import com.ecommerce.user.model.request.UpdateUserProfileRequest;
import com.ecommerce.user.model.request.UserCreateRequest;
import com.ecommerce.user.model.request.UserProfileCreateRequest;
import com.ecommerce.user.model.response.ApiResponse;
import com.ecommerce.user.model.response.UpdateUserProfileResponse;
import com.ecommerce.user.model.response.UserInfoResponse;
import com.ecommerce.user.model.response.UserProfileCreateResponse;
import com.ecommerce.user.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
//@RequestMapping("/user")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @PostMapping("/create")
    public ResponseEntity<UserProfileCreateResponse> createUserProfile(@RequestBody UserProfileCreateRequest userCreateRequest) {
        UserProfileCreateResponse userCreateResponse = userService.createUserProfile(userCreateRequest);
        return new ResponseEntity<>(userCreateResponse, HttpStatus.CREATED);
    }

    @PutMapping("/update-profile")
    public ApiResponse<UpdateUserProfileResponse> updateUserProfile(
            @RequestParam("userId") Long userId,
            @RequestBody UpdateUserProfileRequest request) {

        UpdateUserProfileResponse updatedUser = userService.updateUserProfile(userId, request);
        ApiResponse<UpdateUserProfileResponse> response = new ApiResponse<>();
        response.ok(updatedUser);
        return response;
    }

    @PostMapping("/{userId}/add-address")
    public ApiResponse<Address> addAddressToUser(
            @PathVariable Long userId,
            @RequestBody AddressCreateRequest request) {

        Address savedAddress = userService.addAddressToUser(userId, request);
        ApiResponse<Address> response = new ApiResponse<>();
        response.ok(savedAddress);
        return response;
    }

    @GetMapping("/{userId}/addresses")
    public ApiResponse<List<Address>> getUserAddresses(@PathVariable Long userId) {
        List<Address> addresses = userService.getAddress(userId);
        ApiResponse<List<Address>> response = new ApiResponse<>();
        response.ok(addresses);
        return response;
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserInfoResponse> getUser(@PathVariable Long userId) {
        UserInfoResponse userResponse = userService.getUser(userId);
        ApiResponse<UserInfoResponse> response = new ApiResponse<>();
        response.ok(userResponse);
        return response;
    }
}
