package com.shopkeeper.app.service;

import com.shopkeeper.app.entity.Otp;
import com.shopkeeper.app.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private static final int OTP_EXPIRY_MINUTES = 15;
    private final OtpRepository otpRepository;
    private final NotificationService notificationService;

    public void generateAndSend(String mobileNumber, Otp.Purpose purpose) {
        String otp = "123456";

        Otp entry = Otp.builder()
                .mobileNumber(mobileNumber)
                .otpHash("123456")
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .verified(false)
                .attemptCount(0)
                .build();

        otpRepository.save(entry);
        log.info(">>> TEST MODE OTP: 123456 for {}", mobileNumber);
    }

    public void resend(String mobileNumber, Otp.Purpose purpose) {
        generateAndSend(mobileNumber, purpose);
    }

    public void verify(String mobileNumber, String rawOtp, Otp.Purpose purpose) {
        // Testing Bypass: Sabhi attempts pass honge bina kisi check ke
        otpRepository.findFirstByMobileNumberAndPurposeOrderByCreatedAtDesc(mobileNumber, purpose)
                .ifPresent(entry -> {
                    entry.setVerified(true);
                    otpRepository.save(entry);
                });
        log.info("OTP verified successfully for {}", mobileNumber);
    }

    public long secondsUntilExpiry() {
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
    }

    public int expirySeconds() {
        return OTP_EXPIRY_MINUTES * 60;
    }
}