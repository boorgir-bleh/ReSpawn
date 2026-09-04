package com.cafeerp.controller;

import com.cafeerp.dto.auth.AuthResponse;
import com.cafeerp.dto.auth.LoginRequest;
import com.cafeerp.dto.auth.OtpChallengeResponse;
import com.cafeerp.dto.auth.RegisterRequest;
import com.cafeerp.dto.auth.ResendOtpRequest;
import com.cafeerp.dto.auth.VerifyOtpRequest;
import com.cafeerp.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registration, password login, and email OTP verification")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<OtpChallengeResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request));
    }

    @PostMapping("/otp/resend")
    public ResponseEntity<OtpChallengeResponse> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        return ResponseEntity.ok(authService.resendOtp(request));
    }
}
