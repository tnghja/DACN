package com.ecommerce.inventory.exception;

public class InactivatedUserException extends RuntimeException {
    public InactivatedUserException(String message) {
        super(message);
    }
}
