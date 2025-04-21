package com.ecommerce.identityservice.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.ecommerce.identityservice.validator.DobConstraint;

import jakarta.validation.constraints.Size; // Added for potential password validation
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {

    // Optional: Keep password update here, but handle carefully in service
    // Consider adding size/pattern constraints if kept
    @Size(min = 6, message = "INVALID_PASSWORD") // Example constraint
            String password; // Service should only update if this is not null/blank

    // Allow updating userName
    @Size(min = 4, message = "USERNAME_INVALID") // Example constraint
            String userName;

    // Renamed from dob to match entity field 'dateOfBirth'
    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dateOfBirth;

    // Fields matching User entity
    String fullName;
    String gender;
    String phoneNumber; // Consider adding validation (e.g., @Pattern)

}