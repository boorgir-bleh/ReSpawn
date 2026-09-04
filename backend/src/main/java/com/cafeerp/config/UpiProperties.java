package com.cafeerp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.upi")
public class UpiProperties {

    private String id;
    private String payeeName;
    private String currency;
}
