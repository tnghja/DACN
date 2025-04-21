package com.ecommerce.identityservice.mapper;

import com.ecommerce.identityservice.dto.request.UserCreationRequest;
import com.ecommerce.identityservice.dto.response.RoleResponse; // Import RoleResponse if needed for mapping
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy; // Import for handling nulls

import com.ecommerce.identityservice.dto.request.UserUpdateRequest;
import com.ecommerce.identityservice.dto.response.UserResponse;
import com.ecommerce.identityservice.entity.User;

/**
 * Mapper interface for converting between User entity and its DTOs.
 * Uses MapStruct for code generation.
 */
// Define as a Spring component
// Ignore null values in the source DTO during update operations
// Specify RoleMapper for handling Set<Role> <-> Set<RoleResponse> mapping
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {RoleMapper.class})
public interface UserMapper {

    /**
     * Maps UserCreationRequest DTO to User entity.
     * MapStruct automatically maps fields with the same name (email, password, userName).
     * @param request The user creation request DTO.
     * @return The mapped User entity.
     */
    User toUser(UserCreationRequest request);


    /**
     * Maps User entity to UserResponse DTO.
     * Explicitly maps all relevant fields for clarity and includes fields added for alignment.
     * @param user The user entity.
     * @return The mapped UserResponse DTO.
     */
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "userName", target = "userName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    UserResponse toUserResponse(User user);


    /**
     * Updates an existing User entity from a UserUpdateRequest DTO.
     * Ignores fields that should not be updated directly via this mapping
     * (e.g., password, roles, email, userId, addresses) as they are typically
     * handled by specific logic in the service layer or are immutable.
     * Null fields in the request DTO will be ignored due to NullValuePropertyMappingStrategy.IGNORE.
     *
     * @param user The target User entity to update.
     * @param request The source UserUpdateRequest DTO containing update data.
     */
    // Ignore fields handled separately or that shouldn't be updated

    @Mapping(target = "password", ignore = true) // Password encoding/update is conditional in service
    @Mapping(target = "email", ignore = true) // Email change usually requires verification
    @Mapping(target = "userId", ignore = true) // Primary key should not be updated
    @Mapping(target = "addresses", ignore = true) // Address management likely needs separate API/logic

    // Map fields from request DTO to user entity
    @Mapping(source = "userName", target = "userName")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth") // Maps the renamed field from DTO
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
