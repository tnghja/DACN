package com.ecommerce.user.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileCreateRequest {


    @Email(message = "INVALID_EMAIL")
    @NotBlank(message = "EMAIL_REQUIRED")
    @Size(max = 255, message = "EMAIL_TOO_LONG")
    String email;

    @Size(min = 4, max = 50, message = "USERNAME_INVALID")
    @NotBlank(message = "USERNAME_REQUIRED")
    String userName;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @Size(min = 8, message = "PASSWORD_TOO_SHORT")
    String password;

    @NotBlank(message = "NAME_REQUIRED")
    @Size(max = 100, message = "NAME_TOO_LONG")
    String name;
}
