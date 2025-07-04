package com.ecommerce.product.handler;


import com.ecommerce.product.exception.NotFoundException;
import com.ecommerce.product.exception.ValidationException;
import com.ecommerce.product.model.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ApiResponse handleNotFoundException(NotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("errorCode", "404");
        String errorMessage = (ex.getMessage() == null) ? "NOT_FOUND" : ex.getMessage();
        error.put("errorMessage", errorMessage);

        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.error(error);
        return apiResponse;
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiResponse handleValidationException(ValidationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("errorCode", "400");
        error.put("errorMessage", "BAD_REQUEST");
        error.put("details", ex.getMessage());

        if (ex.getErrors() != null) {
            error.putAll(ex.getErrors());
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.error(error);
        return apiResponse;
    }

//    @ExceptionHandler(ApplicationException.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ResponseBody
//    public ApiResponse handleApplicationException(ApplicationException ex) {
//        Map<String, String> error = new HashMap<>();
//        error.put("errorCode", "500");
//        error.put("errorMessage", ex.getMessage());
//
//        ApiResponse apiResponse = new ApiResponse();
//        apiResponse.error(error);
//        return apiResponse;
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ResponseBody
//    public ApiResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
//        BindingResult bindingResult = ex.getBindingResult();
//        Map<String, String> error = new HashMap<>();
//        error.put("errorCode", "400");
//        error.put("errorMessage", "VALIDATION_FAILED");
//
//        for (FieldError fieldError : bindingResult.getFieldErrors()) {
//            error.put(fieldError.getField(), fieldError.getDefaultMessage());
//        }
//
//        ApiResponse apiResponse = new ApiResponse();
//        apiResponse.error(error);
//        return apiResponse;
//    }
//
//    @ExceptionHandler(IOException.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ResponseBody
//    public ApiResponse handleIOException(IOException ex) {
//        Map<String, String> error = new HashMap<>();
//        error.put("errorCode", "500");
//        error.put("errorMessage", "IO_ERROR");
//        error.put("details", ex.getMessage());
//
//        ApiResponse apiResponse = new ApiResponse();
//        apiResponse.error(error);
//        return apiResponse;
//    }
//
//    @ExceptionHandler(InvalidFileTypeException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ResponseBody
//    public ApiResponse handleInvalidFileTypeException(InvalidFileTypeException ex) {
//        Map<String, String> error = new HashMap<>();
//        error.put("errorCode", "400");
//        error.put("errorMessage", "INVALID_FILE_TYPE");
//        error.put("details", ex.getMessage());
//
//        ApiResponse apiResponse = new ApiResponse();
//        apiResponse.error(error);
//        return apiResponse;
//    }
//
//    @ExceptionHandler(SearchOptionsException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ResponseBody
//    public ApiResponse handleSearchOptionsException(SearchOptionsException ex) {
//        Map<String, String> error = new HashMap<>();
//        error.put("errorCode", "400");
//        error.put("errorMessage", "INVALID_SEARCH_OPTION");
//        error.put("details", ex.getMessage());
//
//        ApiResponse apiResponse = new ApiResponse();
//        Map<String, Object> responseMetadata = new HashMap<>();
//        responseMetadata.put("searchOptions", CourseSearchOptions.HINTS_MAP);
//        apiResponse.error(error,responseMetadata);
//        return apiResponse;
//    }
//    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
//    @ExceptionHandler(MaxUploadSizeExceededException.class)
//    public ApiResponse handleMaxSizeException(MaxUploadSizeExceededException ex) {
//        Map<String, String> error = new HashMap<>();
//        error.put("errorCode", "400");
//        error.put("errorMessage", "MAX_UPLOAD_SIZE_EXCEEDED");
//        error.put("details", ex.getMessage());
//
//        ApiResponse apiResponse = new ApiResponse();
//        apiResponse.error(error);
//        return apiResponse;
//    }
//
//    @ExceptionHandler(DuplicatePhoneNumberException.class)
//    public ApiResponse handleDuplicatePhoneNumberException(DuplicatePhoneNumberException ex) {
//        Map<String, String> error = new HashMap<>();
//        error.put("errorCode", "409");
//        error.put("errorMessage", "CONFLICT");
//        error.put("details", ex.getMessage());
//        ApiResponse apiResponse = new ApiResponse();
//        apiResponse.error(error);
//        return    apiResponse;
//    }
}
