package com.ecommerce.inventory.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {
    private String userName;
    private String password;
    private String email;
}
