package com.ecommerce.user.model.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateUserProfileRequest {
    private String fullName;
    private String gender; // Nam or Nữ
    private String phoneNumber;
    private String email;
    private LocalDate dateOfBirth;
}