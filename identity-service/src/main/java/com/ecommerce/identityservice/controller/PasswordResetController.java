package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.dto.request.PasswordResetRequest;
import com.ecommerce.identityservice.dto.request.PasswordResetSubmitRequest;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.dto.response.ResponseCode;
import com.ecommerce.identityservice.service.PasswordResetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password-reset")
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "bearerAuth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Boolean>> requestReset(
            @Valid @RequestBody PasswordResetRequest emailRequest,
            BindingResult bindingResult) {
        ApiResponse<Boolean> apiResponse = new ApiResponse<>();

        if (bindingResult.hasErrors()) {
            apiResponse.error(ResponseCode.getError(1));
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }

        try {
            boolean result = passwordResetService.requestPasswordReset(emailRequest.getEmail());
            if (result) {
                apiResponse.ok(true);
                return new ResponseEntity<>(apiResponse, HttpStatus.OK);
            } else {
                apiResponse.error(ResponseCode.getError(8));
                return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            apiResponse.error(ResponseCode.getError(23));
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestParam String token) {
        ApiResponse<Boolean> apiResponse = new ApiResponse<>();
        try {
            boolean isValid = passwordResetService.validateResetToken(token);
            apiResponse.ok(isValid);
            return new ResponseEntity<>(apiResponse, isValid ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            apiResponse.error(ResponseCode.getError(23));
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<Boolean>> submitReset(
            @Valid @RequestBody PasswordResetSubmitRequest resetRequest,
            BindingResult bindingResult) {
        ApiResponse<Boolean> apiResponse = new ApiResponse<>();

        if (bindingResult.hasErrors()) {
            apiResponse.error(ResponseCode.getError(1));
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }

        try {
            boolean result = passwordResetService.resetPassword(
                    resetRequest.getToken(),
                    resetRequest.getNewPassword());

            if (result) {
                apiResponse.ok(true);
                return new ResponseEntity<>(apiResponse, HttpStatus.OK);
            } else {
                apiResponse.error(ResponseCode.getError(3));
                return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
            }
        } catch (Exception e) {
            apiResponse.error(ResponseCode.getError(23));
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}