package com.ecommerce.order.model.request;

import lombok.Data;

@Data
public class OrderDetailRequest {
    // Billing details
    private String firstName;
    private String lastName;
    private String country;
    private String streetAddress;
    private String city;

    private Double subtotal;
    private String shipping;
    private Double vat;
    private Double total;

    // Payment method
    private String paymentMethod;
}
