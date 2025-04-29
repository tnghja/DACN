package com.ecommerce.identityservice.dto.response;

import java.time.LocalDate;
import java.util.Set;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    // Existing fields
    String userId;
    String userName;
    String email;


    String fullName;
    String gender;
    String phoneNumber;
    LocalDate dateOfBirth;

}

