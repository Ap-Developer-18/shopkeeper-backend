package com.shopkeeper.app.repository;

import com.shopkeeper.app.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    // latest OTP for a mobile number + purpose, used for validation and resend-cooldown checks
    Optional<Otp> findFirstByMobileNumberAndPurposeOrderByCreatedAtDesc(String mobileNumber, Otp.Purpose purpose);

    List<Otp> findByMobileNumberAndPurposeAndVerifiedFalse(String mobileNumber, Otp.Purpose purpose);
}
