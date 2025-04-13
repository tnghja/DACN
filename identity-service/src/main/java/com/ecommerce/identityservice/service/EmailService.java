package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.entity.User;
import com.ecommerce.identityservice.exception.AppException;
import com.ecommerce.identityservice.exception.ErrorCode;
import com.ecommerce.identityservice.exception.NotFoundException;
import com.ecommerce.identityservice.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
@Service
@RequiredArgsConstructor
public class EmailService {


    private final JavaMailSender mailSender;


    private final MailProperties mailProperties;


    private final TemplateEngine templateEngine;


    private final AuthenticationService authenticationService;


    private final UserRepository userRepository;

    @Value("${mail.backend_host}")
    private String PREFIX;

    private static class MailSenderRunnable implements Runnable {
        private final JavaMailSender mailSender;
        private final MimeMessage mimeMessage;

        public MailSenderRunnable(JavaMailSender mailSender, MimeMessage mimeMessage) {
            this.mailSender = mailSender;
            this.mimeMessage = mimeMessage;
        }

        public void run() {
            try {
                mailSender.send(mimeMessage);
            } catch (Exception ignored) {
            }
        }
    }

    public Boolean sendEmailVerification(String to) {
        try {
            // Get or create user (implementation depends on your business logic)
            User user = userRepository.findByEmail(to)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            // Generate token using AuthenticationService
            String token = authenticationService.generateToken(user);

            String subject = "Xác nhận địa chỉ email của bạn";
            String body1 = "Để xác thực địa chỉ email đã đăng ký vui lòng ấn";
            String body2 = "";
            String link = PREFIX + "/api/v1/auth/verify-email?token=" + token;

            return sendHtmlEmailWithButton(to, subject, body1, body2, link);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean sendPasswordResetEmail(String to) {
        try {
            User user = userRepository.findByEmail(to)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

            String token = authenticationService.generateToken(user);

            String subject = "Xác thực yêu cầu đặt lại mật khẩu";
            String body1 = "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Để tiếp tục quá trình này, vui lòng nhấp vào nút dưới đây:";
            String body2 = "Nếu bạn không yêu cầu đặt lại mật khẩu, bạn có thể bỏ qua email này.";
            String link = PREFIX + "/api/password-reset/confirm?token=" + token;

            return sendHtmlEmailWithButton(to, subject, body1, body2, link);
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean sendPasswordChangedNotification(String to) {
        try {
            String subject = "Mật khẩu đã được thay đổi";
            String body1 = "Mật khẩu cho tài khoản của bạn vừa được thay đổi.";
            String body2 = "Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ với bộ phận hỗ trợ ngay lập tức.";

            return sendHtmlEmailWithoutButton(to, subject, body1, body2);
        } catch (Exception e) {
            return false;
        }
    }

    private Boolean sendHtmlEmailWithButton(String to, String subject, String body1, String body2, String link) {
        try {
            Context context = new Context();
            context.setVariable("body1", body1);
            context.setVariable("body2", body2);
            context.setVariable("link", link);
            context.setVariable("greetings", "Kính gửi " + to + ",");

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(mailProperties.getUsername(), "Your App Name");
            helper.setTo(to);
            helper.setSubject(subject);
            String htmlContent = templateEngine.process("email-with-button", context);
            helper.setText(htmlContent, true);

            new Thread(new MailSenderRunnable(mailSender, mimeMessage)).start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Boolean sendHtmlEmailWithoutButton(String to, String subject, String body1, String body2) {
        try {
            Context context = new Context();
            context.setVariable("body1", body1);
            context.setVariable("body2", body2);
            context.setVariable("greetings", "Kính gửi " + to + ",");

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(mailProperties.getUsername(), "Your App Name");
            helper.setTo(to);
            helper.setSubject(subject);
            String htmlContent = templateEngine.process("email-without-button", context);
            helper.setText(htmlContent, true);

            new Thread(new MailSenderRunnable(mailSender, mimeMessage)).start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}