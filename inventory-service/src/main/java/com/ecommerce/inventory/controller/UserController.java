//package com.ecommerce.inventory.controller;
//
//import com.ecommerce.inventory.model.entity.Address;
//import com.ecommerce.inventory.model.entity.User;
//import com.ecommerce.inventory.model.request.AddressCreateRequest;
//import com.ecommerce.inventory.model.request.UpdateUserProfileRequest;
//import com.ecommerce.inventory.model.request.UserProfileCreateRequest;
//import com.ecommerce.inventory.model.response.ApiResponse;
//import com.ecommerce.inventory.model.response.UpdateUserProfileResponse;
//import com.ecommerce.inventory.model.response.UserInfoResponse;
//import com.ecommerce.inventory.model.response.UserProfileCreateResponse;
//import com.ecommerce.inventory.service.UserService;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/user")
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class UserController {
//
//    UserService userService;
//
//    @PostMapping("/create")
//    public ResponseEntity<UserProfileCreateResponse> createUserProfile(@RequestBody UserProfileCreateRequest userCreateRequest) {
//        UserProfileCreateResponse userCreateResponse = userService.createUserProfile(userCreateRequest);
//        return new ResponseEntity<>(userCreateResponse, HttpStatus.CREATED);
//    }
//
//    @PutMapping("/update-profile")
//    public ApiResponse<UpdateUserProfileResponse> updateUserProfile(
//            @RequestParam("userId") Long userId,
//            @RequestBody UpdateUserProfileRequest request) {
//
//        UpdateUserProfileResponse updatedUser = userService.updateUserProfile(userId, request);
//        ApiResponse<UpdateUserProfileResponse> response = new ApiResponse<>();
//        response.ok(updatedUser);
//        return response;
//    }
//
//    @PostMapping("/{userId}/add-address")
//    public ApiResponse<Address> addAddressToUser(
//            @PathVariable Long userId,
//            @RequestBody AddressCreateRequest request) {
//
//        Address savedAddress = userService.addAddressToUser(userId, request);
//        ApiResponse<Address> response = new ApiResponse<>();
//        response.ok(savedAddress);
//        return response;
//    }
//
//    @GetMapping("/{userId}/addresses")
//    public ApiResponse<List<Address>> getUserAddresses(@PathVariable Long userId) {
//        List<Address> addresses = userService.getAddress(userId);
//        ApiResponse<List<Address>> response = new ApiResponse<>();
//        response.ok(addresses);
//        return response;
//    }
//
//    @GetMapping("/{userId}")
//    public ApiResponse<UserInfoResponse> getUser(@PathVariable Long userId) {
//        UserInfoResponse userResponse = userService.getUser(userId);
//        ApiResponse<UserInfoResponse> response = new ApiResponse<>();
//        response.ok(userResponse);
//        return response;
//    }
//}
