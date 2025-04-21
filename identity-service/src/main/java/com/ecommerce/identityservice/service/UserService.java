package com.ecommerce.identityservice.service;

import java.util.HashSet;
import java.util.List;

import com.ecommerce.identityservice.constant.PredefinedRole;
import com.ecommerce.identityservice.dto.request.UserCreationRequest;
import com.ecommerce.identityservice.dto.request.UserProfileCreateRequest;
import com.ecommerce.identityservice.exception.AppException;
import com.ecommerce.identityservice.exception.ErrorCode;
import com.ecommerce.identityservice.mapper.UserMapper;
import com.ecommerce.identityservice.repository.RoleRepository;
import com.ecommerce.identityservice.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.identityservice.dto.request.UserUpdateRequest;
import com.ecommerce.identityservice.dto.response.UserResponse;
import com.ecommerce.identityservice.entity.Role;
import com.ecommerce.identityservice.entity.User;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {
        // Email format
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new AppException(ErrorCode.INVALID_EMAIL);
        }
        // Password length
        if (request.getPassword().length() < 6) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }
        // Username length
        if (request.getUserName().length() < 4) {
            throw new AppException(ErrorCode.USERNAME_INVALID);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);

        try {
            user = userRepository.save(user);
            UserProfileCreateRequest profileCreateRequest = new UserProfileCreateRequest();
            profileCreateRequest.setUserName(request.getUserName());
            profileCreateRequest.setUserId(user.getUserId());
            profileCreateRequest.setEmail(request.getEmail());
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String id = context.getAuthentication().getName();

        User user = userRepository.findByUserId(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

// Map fields from request (excluding password and roles) using the mapper
        userMapper.updateUser(user, request);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            log.debug("Updating password for user {}", userId); // Add logging
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }
//    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }
}
