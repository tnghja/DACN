package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.entity.User;
import com.ecommerce.identityservice.exception.AppException;
import com.ecommerce.identityservice.exception.ErrorCode;
import com.ecommerce.identityservice.repository.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final EmailService emailService;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // In-memory store for reset tokens and their expiration
    private final ConcurrentHashMap<String, PasswordResetToken> resetTokens = new ConcurrentHashMap<>();

    private static class PasswordResetToken {
        @Getter
        private final String email;
        private final Date expiryDate;

        public PasswordResetToken(String email, Date expiryDate) {
            this.email = email;
            this.expiryDate = expiryDate;
        }

        public boolean isExpired() {
            return new Date().after(expiryDate);
        }
    }

    public boolean requestPasswordReset(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            // Generate a unique token
            String token = UUID.randomUUID().toString();

            // Set expiry time (e.g., 1 hour from now)
            Date expiryDate = new Date(System.currentTimeMillis() + 3600000); // 1 hour

            // Store the token
            resetTokens.put(token, new PasswordResetToken(email, expiryDate));

            // Send email with the reset link
            return emailService.sendPasswordResetEmail(email, token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateResetToken(String token) {
        PasswordResetToken resetToken = resetTokens.get(token);
        if (resetToken == null || resetToken.isExpired()) {
            return false;
        }
        return true;
    }

    public boolean resetPassword(String token, String newPassword) {
        try {
            PasswordResetToken resetToken = resetTokens.get(token);
            if (resetToken == null || resetToken.isExpired()) {
                return false;
            }

            String email = resetToken.getEmail();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Remove the used token
            resetTokens.remove(token);

            // Send notification email
            emailService.sendPasswordChangedNotification(email);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Additional method to clear expired tokens periodically
    public void cleanExpiredTokens() {
        resetTokens.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
}