package com.cafeerp.dto.auth;

public record OtpChallengeResponse(
        String email,
        String message,
        long expiresInSeconds
) {
}
