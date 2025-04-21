package com.ecommerce.inventory.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressCreateRequest {
    private String street;
    private String city;
    private String province;
    private String country;
}
