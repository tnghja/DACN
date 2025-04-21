package com.ecommerce.order.exception;

public class InactivatedUserException extends RuntimeException {
    public InactivatedUserException(String message) {
        super(message);
    }
}
