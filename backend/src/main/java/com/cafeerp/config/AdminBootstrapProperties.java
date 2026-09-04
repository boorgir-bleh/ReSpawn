package com.cafeerp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.admin-bootstrap")
public class AdminBootstrapProperties {

    private String email;
    private String password;
    private String name;
    private String phone;
}
