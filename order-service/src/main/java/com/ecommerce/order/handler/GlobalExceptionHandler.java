package com.ecommerce.order.handler;

import com.ecommerce.order.exception.*;
import com.ecommerce.order.model.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, String> createErrorBody(Exception ex, HttpStatus status) {
        Map<String, String> error = new HashMap<>();
        error.put("errorCode", String.valueOf(status.value()));
        error.put("errorMessage", status.getReasonPhrase());
        if (ex.getMessage() != null) {
            error.put("details", ex.getMessage());
        }
        return error;
    }

    private Map<String, String> createErrorBodyWithDetails(Exception ex, HttpStatus status, Map<String, String> additionalDetails) {
        Map<String, String> error = createErrorBody(ex, status);
        if (additionalDetails != null) {
            error.putAll(additionalDetails);
        }
        return error;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception ex) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.error(createErrorBody(ex, HttpStatus.INTERNAL_SERVER_ERROR));
        return response;
    }

    @ExceptionHandler({NotFoundException.class, ProductNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFoundException(RuntimeException ex) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.error(createErrorBody(ex, HttpStatus.NOT_FOUND));
        return response;
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(ValidationException ex) {
        ApiResponse<Void> response = new ApiResponse<>();
        Map<String, String> additionalDetails = new HashMap<>();
        if (ex.getErrors() != null) {
            additionalDetails.putAll(ex.getErrors());
        }
        response.error(createErrorBodyWithDetails(ex, HttpStatus.BAD_REQUEST, additionalDetails));
        return response;
    }

    @ExceptionHandler(InventoryException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleInventoryException(InventoryException ex) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.error(createErrorBody(ex, HttpStatus.BAD_REQUEST));
        return response;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(fieldName, message);
        });

        ApiResponse<Void> response = new ApiResponse<>();
        response.error(createErrorBodyWithDetails(ex, HttpStatus.BAD_REQUEST, fieldErrors));
        return response;
    }
}