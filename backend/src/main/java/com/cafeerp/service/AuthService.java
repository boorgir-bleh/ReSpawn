package com.cafeerp.service;

import com.cafeerp.dto.auth.AuthResponse;
import com.cafeerp.dto.auth.LoginRequest;
import com.cafeerp.dto.auth.OtpChallengeResponse;
import com.cafeerp.dto.auth.RegisterRequest;
import com.cafeerp.dto.auth.ResendOtpRequest;
import com.cafeerp.dto.auth.VerifyOtpRequest;
import com.cafeerp.entity.Role;
import com.cafeerp.entity.User;
import com.cafeerp.exception.BadRequestException;
import com.cafeerp.exception.ConflictException;
import com.cafeerp.repository.UserRepository;
import com.cafeerp.security.JwtService;
import com.cafeerp.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    @Transactional
    public OtpChallengeResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .enabled(true)
                .build();

        user = userRepository.save(user);
        return otpService.issueOtp(user);
    }

    public OtpChallengeResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new IllegalStateException("User vanished after successful authentication"));

        return otpService.issueOtp(user);
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification code"));

        otpService.verifyOtp(user, request.code());

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);

        return AuthResponse.bearer(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }

    @Transactional
    public OtpChallengeResponse resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadRequestException("No account found with this email"));

        return otpService.issueOtp(user);
    }
}
