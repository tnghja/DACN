package com.ecommerce.identityservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequest {
    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Vui lòng điền email")
    private String email;
}
