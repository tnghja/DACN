package com.ecommerce.user.service.impl;


import com.ecommerce.user.exception.ApplicationException;
import com.ecommerce.user.exception.NotFoundException;
import com.ecommerce.user.model.entity.Address;
import com.ecommerce.user.model.entity.User;
import com.ecommerce.user.model.mapper.AddressMapper;
import com.ecommerce.user.model.mapper.UserMapper;
import com.ecommerce.user.model.request.AddressCreateRequest;
import com.ecommerce.user.model.request.UpdateUserProfileRequest;

import com.ecommerce.user.model.response.UpdateUserProfileResponse;
import com.ecommerce.user.model.response.UserProfileCreateResponse;


import com.ecommerce.user.model.response.UserInfoResponse;
import com.ecommerce.user.model.request.UserProfileCreateRequest;
import com.ecommerce.user.repository.AddressRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserMapper userMapper;
    @Autowired
    AddressRepository addressRepository;
    @Autowired
    AddressMapper addressMapper;

    @Override
    public UserProfileCreateResponse createUserProfile(UserProfileCreateRequest userCreateRequest) {
        // Check for existing email or username
        if (userRepository.existsByEmail(userCreateRequest.getEmail())) {
            throw new ApplicationException("This email address already exists");
        }

        if (userRepository.existsByUserName(userCreateRequest.getUserName())) {
            throw new ApplicationException("This username already exists");
        }
        userCreateRequest.setName("11111");
        // Create new user entity
        User newUser = userMapper.toEntity(userCreateRequest);

        try {
            // Save new user in the repository
            userRepository.save(newUser);
            // Return response using mapper
            return userMapper.toUserCreateResponse(newUser);
        } catch (Exception ex) {

            throw new ApplicationException("Error creating user profile" + ex.getMessage());
        }
    }


    public UpdateUserProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));

        // Update user profile based on request
        user.setFullName(request.getFullName());
        user.setGender(request.getGender());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setDateOfBirth(request.getDateOfBirth());

        // Save updated user
        userRepository.save(user);

        // Map User entity to UpdateUserProfileResponse
        return userMapper.toUpdateUserProfileResponse(user);
    }

    public Address addAddressToUser(Long userId, AddressCreateRequest request) {
        // Tìm người dùng theo userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));

        // Sử dụng AddressMapper để ánh xạ từ AddressCreateRequest sang Address
        Address newAddress = addressMapper.toEntity(request);

        // Gán người dùng cho địa chỉ
        newAddress.setUser(user);

        // Lưu địa chỉ mới vào DB
        return addressRepository.save(newAddress);
    }

    public List<Address> getAddress(Long userId) {
        // Tìm người dùng theo userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));

        // Trả về danh sách địa chỉ của người dùng
        return user.getAddresses();
    }

    public UserInfoResponse getUser(Long userId) {
        // Tìm người dùng theo userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));

        // Sử dụng UserMapper để ánh xạ từ User entity sang UserResponse DTO
        return userMapper.toUserInfoResponse(user);
    }
}

