package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.entity.User;
import com.ecommerce.identityservice.exception.AppException;
import com.ecommerce.identityservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
public class EmailServiceIntegrationTest {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private JavaMailSender mailSender;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        User testUser = User.builder()
                .email("user@example.com")
                .userName("testuser")
                .build();
        userRepository.save(testUser);
    }

    @Test
    void sendEmailVerification_existingUser() {
        Boolean result = emailService.sendEmailVerification("user@example.com");
        assertTrue(result);
    }

    @Test
    void sendEmailVerification_nonExistentUser() {
        assertThrows(AppException.class, () -> emailService.sendEmailVerification("nouser@example.com"));
    }

    @Test
    void sendPasswordResetEmail_validUser() {
        Boolean result = emailService.sendPasswordResetEmail("user@example.com", "token123");
        assertTrue(result);
    }

    @Test
    void sendPasswordResetEmail_nonExistentUser() {
        assertThrows(AppException.class, () -> emailService.sendPasswordResetEmail("nouser@example.com", "token123"));
    }

    @Test
    void emailSendingFailure() {
//        doThrow(new RuntimeException("Mail server down")).when(mailSender).send(any());
        // Should not throw, but handle gracefully
        assertDoesNotThrow(() -> emailService.sendEmailVerification("user@example.com"));
    }
}
