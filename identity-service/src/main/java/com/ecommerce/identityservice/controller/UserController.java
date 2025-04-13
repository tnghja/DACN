package com.ecommerce.identityservice.controller;


import com.ecommerce.identityservice.dto.request.UserCreationRequest;
import com.ecommerce.identityservice.dto.request.UserUpdateRequest;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.entity.Address;
import com.ecommerce.identityservice.dto.response.UserResponse;
import com.ecommerce.identityservice.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {
    UserService userService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UserResponse>> createUserProfile(@RequestBody @Valid UserCreationRequest request) {
        UserResponse userResponse = userService.createUser(request);
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.ok(userResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers() {
        List<UserResponse> users = userService.getUsers();
        ApiResponse<List<UserResponse>> response = new ApiResponse<>();
        response.ok(users);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable String userId) {
        UserResponse userResponse = userService.getUser(userId);
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.ok(userResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-info")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo() {
        UserResponse user = userService.getMyInfo();
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.ok(user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        ApiResponse<String> response = new ApiResponse<>();
        response.ok("User has been deleted");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @RequestParam("userId") String userId,
            @RequestBody UserUpdateRequest request) {
        UserResponse updatedUser = userService.updateUser(userId, request);
        ApiResponse<UserResponse> response = new ApiResponse<>();
        response.ok(updatedUser);
        return ResponseEntity.ok(response);
    }

//    @PostMapping("/{userId}/add-address")
//    public ResponseEntity<ApiResponse<Address>> addAddressToUser(
//            @PathVariable String userId,
//            @RequestBody Address request) {
//        Address savedAddress = userService.addAddressToUser(userId, request);
//        ApiResponse<Address> response = new ApiResponse<>();
//        response.ok(savedAddress);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }
//
//    @GetMapping("/{userId}/addresses")
//    public ResponseEntity<ApiResponse<List<Address>>> getUserAddresses(@PathVariable String userId) {
//        List<Address> addresses = userService.getAddresses(userId);
//        ApiResponse<List<Address>> response = new ApiResponse<>();
//        response.ok(addresses);
//        return ResponseEntity.ok(response);
//    }
}