package com.ecommerce.order.error_handler;

import com.ecommerce.order.exception.InventoryException;
import com.ecommerce.order.exception.NotFoundException;
import com.ecommerce.order.exception.ProductNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class UserErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new NotFoundException("User not found");
            case 400 -> new InventoryException("Invalid inventory request");
            default -> new RuntimeException("Unexpected error from product-service");
        };
    }
}
