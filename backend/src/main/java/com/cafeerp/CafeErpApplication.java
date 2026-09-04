package com.cafeerp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CafeErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(CafeErpApplication.class, args);
    }
}
