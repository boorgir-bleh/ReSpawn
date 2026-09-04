package com.cafeerp.service;

import com.cafeerp.config.OtpProperties;
import com.cafeerp.dto.auth.OtpChallengeResponse;
import com.cafeerp.entity.EmailOtp;
import com.cafeerp.entity.User;
import com.cafeerp.exception.BadRequestException;
import com.cafeerp.exception.ConflictException;
import com.cafeerp.repository.EmailOtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final EmailOtpRepository emailOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpProperties otpProperties;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public OtpChallengeResponse issueOtp(User user) {
        emailOtpRepository.findFirstByUserOrderByCreatedAtDesc(user).ifPresent(last -> {
            boolean stillFresh = last.getCreatedAt()
                    .plusSeconds(otpProperties.getResendCooldownSeconds())
                    .isAfter(Instant.now());
            if (!last.isConsumed() && stillFresh) {
                throw new ConflictException("Please wait before requesting another code");
            }
        });

        int bound = (int) Math.pow(10, otpProperties.getLength());
        String code = String.format("%0" + otpProperties.getLength() + "d", random.nextInt(bound));

        EmailOtp otp = EmailOtp.builder()
                .user(user)
                .codeHash(passwordEncoder.encode(code))
                .expiresAt(Instant.now().plusSeconds(otpProperties.getExpirationSeconds()))
                .consumed(false)
                .build();
        emailOtpRepository.save(otp);

        log.debug("OTP for {}: {}", user.getEmail(), code);
        emailService.sendOtpEmail(user.getEmail(), code, otpProperties.getExpirationSeconds());

        return new OtpChallengeResponse(
                user.getEmail(),
                "A verification code has been sent to your email",
                otpProperties.getExpirationSeconds()
        );
    }

    @Transactional
    public void verifyOtp(User user, String code) {
        EmailOtp otp = emailOtpRepository.findFirstByUserAndConsumedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification code"));

        if (otp.getExpiresAt().isBefore(Instant.now()) || !passwordEncoder.matches(code, otp.getCodeHash())) {
            throw new BadRequestException("Invalid or expired verification code");
        }

        otp.setConsumed(true);
    }
}
