package com.shopkeeper.app.service;

import com.shopkeeper.app.entity.Otp;
import com.shopkeeper.app.exception.ApiException;
import com.shopkeeper.app.exception.InvalidOTPException;
import com.shopkeeper.app.exception.OTPAttemptExceededException;
import com.shopkeeper.app.exception.OTPExpiredException;
import com.shopkeeper.app.repository.OtpRepository;
import com.shopkeeper.app.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Handles OTP lifecycle: generation, hashed storage, expiry, retry-limit and
 * resend-cooldown enforcement. The raw OTP value is only ever held in memory
 * long enough to hash it and hand it to NotificationService - it is never
 * persisted or logged in plaintext.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    /** Generates, stores (hashed), and dispatches a new OTP - invalidating any prior unverified one. */
    public void generateAndSend(String mobileNumber, Otp.Purpose purpose) {
        String otp = OtpGenerator.generate6Digit();

        Otp entry = Otp.builder()
                .mobileNumber(mobileNumber)
                .otpHash(passwordEncoder.encode(otp))
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .verified(false)
                .attemptCount(0)
                .build();

        otpRepository.save(entry);
        notificationService.sendOtp(mobileNumber, otp, purpose.name());
        log.info("OTP generated for purpose={} (mobile number withheld from logs)", purpose);
    }

    /** Enforces the 60-second resend cooldown, then invalidates the old OTP and issues a new one. */
    public void resend(String mobileNumber, Otp.Purpose purpose) {
        otpRepository.findFirstByMobileNumberAndPurposeOrderByCreatedAtDesc(mobileNumber, purpose)
                .ifPresent(last -> {
                    long secondsSinceLast = Duration.between(last.getCreatedAt(), LocalDateTime.now()).getSeconds();
                    if (secondsSinceLast < RESEND_COOLDOWN_SECONDS) {
                        throw new ApiException(
                                "Please wait " + (RESEND_COOLDOWN_SECONDS - secondsSinceLast) + " seconds before requesting a new OTP");
                    }
                });
        generateAndSend(mobileNumber, purpose);
    }

    /** Validates the OTP: checks existence, expiry, and attempt count, then marks it verified. */
    public void verify(String mobileNumber, String rawOtp, Otp.Purpose purpose) {
        Otp entry = otpRepository.findFirstByMobileNumberAndPurposeOrderByCreatedAtDesc(mobileNumber, purpose)
                .orElseThrow(() -> new InvalidOTPException("No OTP was requested for this mobile number"));

        if (entry.isVerified()) {
            throw new InvalidOTPException("This OTP has already been used");
        }

        if (entry.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new OTPAttemptExceededException("Maximum OTP verification attempts exceeded. Please request a new OTP.");
        }

        if (LocalDateTime.now().isAfter(entry.getExpiresAt())) {
            throw new OTPExpiredException("OTP has expired. Please request a new one.");
        }

        boolean matches = passwordEncoder.matches(rawOtp, entry.getOtpHash());
        entry.setAttemptCount(entry.getAttemptCount() + 1);

        if (!matches) {
            otpRepository.save(entry);
            throw new InvalidOTPException("Incorrect OTP");
        }

        entry.setVerified(true);
        otpRepository.save(entry);
    }

    public long secondsUntilExpiry() {
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
    }

    public int expirySeconds() {
        return OTP_EXPIRY_MINUTES * 60;
    }
}
