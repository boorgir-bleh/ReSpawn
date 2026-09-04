package com.cafeerp.repository;

import com.cafeerp.entity.EmailOtp;
import com.cafeerp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findFirstByUserAndConsumedFalseOrderByCreatedAtDesc(User user);

    Optional<EmailOtp> findFirstByUserOrderByCreatedAtDesc(User user);
}
