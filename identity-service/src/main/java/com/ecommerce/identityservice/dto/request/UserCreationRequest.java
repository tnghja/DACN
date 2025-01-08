package com.ecommerce.identityservice.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.ecommerce.identityservice.validator.DobConstraint;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Email(message = "INVALID_EMAIL")

    @NotBlank(message = "EMAIL_REQUIRED")
    String email;

    @Size(min = 6, message = "INVALID_PASSWORD")
    @NotBlank(message = "PASSWORD_REQUIRED")
    String password;

    @Size(min = 4, message = "USERNAME_INVALID")
    @NotBlank(message = "USERNAME_REQUIRED")
    String userName;

    // Getters and setters (if needed)
}
