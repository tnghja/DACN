package com.ecommerce.inventory.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileCreateRequest {
    String userId;

    @Email(message = "INVALID_EMAIL")
    @NotBlank(message = "EMAIL_REQUIRED")
    String email;

    @Size(min = 4, message = "USERNAME_INVALID")
    @NotBlank(message = "USERNAME_REQUIRED")
    String userName;

    String password;

    String name = "1111";
}
