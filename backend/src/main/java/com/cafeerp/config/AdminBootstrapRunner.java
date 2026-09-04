package com.cafeerp.config;

import com.cafeerp.entity.Role;
import com.cafeerp.entity.User;
import com.cafeerp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminBootstrapProperties adminBootstrapProperties;

    @Override
    public void run(String... args) {
        String email = adminBootstrapProperties.getEmail();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        User admin = User.builder()
                .fullName(adminBootstrapProperties.getName())
                .email(email)
                .phoneNumber(adminBootstrapProperties.getPhone())
                .passwordHash(passwordEncoder.encode(adminBootstrapProperties.getPassword()))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        userRepository.save(admin);
        log.info("Bootstrap admin account created: {}", email);
    }
}
