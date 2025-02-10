package com.ecommerce.user.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
public class UpdateUserProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name should not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Gender is required")
    @Pattern(regexp = "^[WM]$", message = "Gender must be 'W' or 'M'")
    private String gender; // W or M

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 15, message = "Phone number should be between 10 and 15 characters")
    private String phoneNumber;

//    @Email(message = "Invalid email format")
//    @NotBlank(message = "Email is required")
//    @Size(max = 255, message = "Email should not exceed 255 characters")
//    private String email;

    private LocalDate dateOfBirth;
}