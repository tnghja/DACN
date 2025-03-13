package com.ecommerce.product.model.entity;

public enum UserRole {
    MANAGER,
    CUSTOMER;

    public static class Role {
        public static final String MANAGER = "M";
        public static final String CUSTOMER = "C";
    }
}
