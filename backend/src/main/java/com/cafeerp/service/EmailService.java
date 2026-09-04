package com.cafeerp.service;

import com.cafeerp.config.AppMailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final AppMailProperties appMailProperties;

    public void sendOtpEmail(String toEmail, String code, long expirationSeconds) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(appMailProperties.getFromAddress());
            message.setTo(toEmail);
            message.setSubject("Your verification code");
            message.setText("Your verification code is: " + code
                    + "\nThis code expires in " + (expirationSeconds / 60) + " minutes.");
            mailSender.send(message);
        } catch (MailException ex) {
            log.warn("Failed to send OTP email to {}: {}", toEmail, ex.getMessage());
        }
    }
}
