package com.ecommerce.user.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileCreateResponse {
//    String id;
    String userName;
    String email;
}
