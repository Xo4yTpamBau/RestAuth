package com.sprect.service.mail;

import com.sprect.service.jwt.JwtService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.List;

@Service
public class MailServiceImpl implements MailService {
    private static final String ACTIVATION_EMAIL = "Thanks for signing up with Sprect! You must follow this link within 1 days of registration to activate your account:\n" +
            "\n" +
            "https://sprect.herokuapp.com/auth/confirmationEmail/%s\n" +
            "\n" +
            "Have fun, and don't hesitate to contact us with your feedback.\n" +
            "\n" +
            "The Sprect Team\n" +
            "https://sprect.herokuapp.com/";

    private static final String SUSPICIOUS_ACTIVITY = "Suspicious activity has been detected from your account.\n" +
            "If it's not you, take action.";

    private static final String RESET_PASSWORD = "Someone (hopefully you) has requested a password reset for your Sprect account. Follow the link below to set a new password:\n" +
            "\n" +
            "https://sprect.herokuapp.com/auth/resetPassword/%s\n" +
            "\n" +
            "If you don't wish to reset your password, disregard this email and no action will be taken.\n" +
            "\n" +
            "The Sprect Team\n" +
            "https://sprect.herokuapp.com/";

    @Value("${spring.mail.username}")
    private String username;

    private final JavaMailSender emailSender;
    private final JwtService jwtService;

    public MailServiceImpl(JavaMailSender emailSender,
                           JwtService jwtService) {
        this.emailSender = emailSender;
        this.jwtService = jwtService;
    }

    @Override
    @SneakyThrows
    public void sendActivationCode(String to, String subject) {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(username);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(String.format(ACTIVATION_EMAIL, jwtService.createTokens(to, List.of("confirmationEmail"))));

        emailSender.send(message);
    }

    @Override
    @SneakyThrows
    public void sendSuspiciousActivity(String to, String subject) {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(username);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(SUSPICIOUS_ACTIVITY);

        emailSender.send(message);
    }

    @Override
    @SneakyThrows
    public void sendEmailForChangePassword(String to, String subject) {

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(username);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(String.format(RESET_PASSWORD, jwtService.createTokens(to, List.of("resetPassword"))));

        emailSender.send(message);
    }
}
