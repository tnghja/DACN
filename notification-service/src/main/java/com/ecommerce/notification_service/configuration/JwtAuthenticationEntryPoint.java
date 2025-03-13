package com.ecommerce.notification_service.configuration;

import com.ecommerce.notification_service.exception.ErrorCode;
import com.ecommerce.notification_service.model.response.ApiResponse;
import com.ecommerce.notification_service.model.response.StatusEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.Map;
@Data
@Getter
@Setter
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        // Set the HTTP status code
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Build the API response
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .status(StatusEnum.ERROR)  // Explicitly set the status
                .error(Map.of("code", String.valueOf(errorCode.getCode()), "message", errorCode.getMessage()))
                .build();

        // Write the response
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }
}
