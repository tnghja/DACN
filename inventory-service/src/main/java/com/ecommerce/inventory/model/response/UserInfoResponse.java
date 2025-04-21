package com.ecommerce.inventory.model.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class UserInfoResponse {
    private Long userId;
    private String name;
    private String email;
    private String fullName;
    private String gender;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    // Bạn có thể thêm nhiều thông tin hơn nếu cần
}