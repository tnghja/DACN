package com.ecommerce.identityservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetSubmitRequest {
    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}