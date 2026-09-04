package com.cafeerp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.otp")
public class OtpProperties {

    private int length = 6;
    private long expirationSeconds = 300;
    private long resendCooldownSeconds = 30;
}
