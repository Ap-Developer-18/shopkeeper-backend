package com.shopkeeper.app.service;

import com.shopkeeper.app.config.JwtUtil;
import com.shopkeeper.app.dto.*;
import com.shopkeeper.app.entity.Otp;
import com.shopkeeper.app.entity.PasswordResetToken;
import com.shopkeeper.app.entity.RegistrationToken;
import com.shopkeeper.app.entity.User;
import com.shopkeeper.app.exception.*;
import com.shopkeeper.app.mapper.UserMapper;
import com.shopkeeper.app.repository.PasswordResetTokenRepository;
import com.shopkeeper.app.repository.RegistrationTokenRepository;
import com.shopkeeper.app.repository.UserRepository;
import com.shopkeeper.app.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final int REGISTRATION_TOKEN_TTL_MINUTES = 10;
    private static final int RESET_TOKEN_TTL_MINUTES = 10;

    private final UserRepository userRepository;
    private final RegistrationTokenRepository registrationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final PendingRegistrationCache pendingRegistrationCache;

    // ============================================================
    // STEP 1 - Register: initiate
    // ============================================================
    @Transactional
    public RegisterInitiateResponse initiateRegistration(RegisterInitiateRequest req) {
        if (userRepository.existsByMobileNumber(req.getMobileNumber())) {
            throw new UserAlreadyExistsException("An account with this mobile number already exists");
        }

        pendingRegistrationCache.put(req.getMobileNumber(), req.getName(), req.getCompanyName());
        otpService.generateAndSend(req.getMobileNumber(), Otp.Purpose.REGISTER);
        log.info("Registration initiated for a new mobile number");

        return new RegisterInitiateResponse(req.getMobileNumber(), otpService.expirySeconds());
    }

    // ============================================================
    // STEP 2 - Register: verify OTP
    // ============================================================
    @Transactional
    public VerifyOtpResponse verifyRegistrationOtp(VerifyOtpRequest req) {
        otpService.verify(req.getMobileNumber(), req.getOtp(), Otp.Purpose.REGISTER);

        String[] nameAndCompany = pendingRegistrationCache.getNameAndCompany(req.getMobileNumber());

        String token = TokenGenerator.generate();
        RegistrationToken regToken = RegistrationToken.builder()
                .token(token)
                .mobileNumber(req.getMobileNumber())
                .name(nameAndCompany[0])
                .companyName(nameAndCompany[1])
                .expiryTime(LocalDateTime.now().plusMinutes(REGISTRATION_TOKEN_TTL_MINUTES))
                .used(false)
                .build();
        registrationTokenRepository.save(regToken);
        pendingRegistrationCache.evict(req.getMobileNumber());

        log.info("Mobile number verified, registration token issued");
        return new VerifyOtpResponse(token, REGISTRATION_TOKEN_TTL_MINUTES * 60);
    }

    // ============================================================
    // STEP 3 - Register: complete
    // ============================================================
    @Transactional
    public LoginResponse completeRegistration(CompleteRegistrationRequest req) {
        RegistrationToken regToken = registrationTokenRepository.findByToken(req.getRegistrationToken())
                .orElseThrow(() -> new InvalidRegistrationTokenException("Invalid registration token"));

        if (regToken.isUsed()) {
            throw new InvalidRegistrationTokenException("This registration token has already been used");
        }
        if (LocalDateTime.now().isAfter(regToken.getExpiryTime())) {
            throw new InvalidRegistrationTokenException("Registration token has expired. Please start registration again.");
        }

        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new ApiException("Password and confirm password do not match");
        }

        if (userRepository.existsByUsername(req.getUsername())) {
            throw new UsernameAlreadyExistsException("Username is already taken");
        }
        if (userRepository.existsByMobileNumber(regToken.getMobileNumber())) {
            throw new UserAlreadyExistsException("An account with this mobile number already exists");
        }

        // name/companyName were captured at step 1 and carried on the registration token
        User user = User.builder()
                .name(regToken.getName() != null ? regToken.getName() : req.getUsername())
                .companyName(regToken.getCompanyName())
                .mobileNumber(regToken.getMobileNumber())
                .username(req.getUsername())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .phoneVerified(true)
                .status(User.UserStatus.ACTIVE)
                .build();

        User saved = userRepository.save(user);

        regToken.setUsed(true);
        registrationTokenRepository.save(regToken);

        log.info("Registration completed for a new user account");

        String accessToken = jwtUtil.generateAccessToken(saved.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(saved.getUsername());
        return new LoginResponse(accessToken, refreshToken, "Bearer", UserMapper.toResponse(saved));
    }

    // ============================================================
    // Login
    // ============================================================
    public LoginResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Login failed: invalid credentials");
            throw new InvalidCredentialsException("Invalid username or password");
        }

        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        log.info("User logged in successfully");

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        return new LoginResponse(accessToken, refreshToken, "Bearer", UserMapper.toResponse(user));
    }

    // ============================================================
    // Refresh access token
    // ============================================================
    public LoginResponse refreshAccessToken(String refreshToken) {
        String username;
        try {
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                throw new InvalidCredentialsException("Invalid refresh token");
            }
            username = jwtUtil.extractUsername(refreshToken);
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid or expired refresh token");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        return new LoginResponse(newAccessToken, newRefreshToken, "Bearer", UserMapper.toResponse(user));
    }

    // ============================================================
    // Forgot password - step 1: request OTP
    // ============================================================
    @Transactional
    public void forgotPasswordInitiate(ForgotPasswordRequest req) {
        User user = userRepository.findByMobileNumber(req.getMobileNumber())
                .orElseThrow(() -> new ResourceNotFoundException("No account found for this mobile number"));

        otpService.generateAndSend(user.getMobileNumber(), Otp.Purpose.RESET_PASSWORD);
        log.info("Password reset OTP requested");
    }

    // ============================================================
    // Forgot password - step 2: verify OTP
    // ============================================================
    @Transactional
    public PasswordResetTokenResponse forgotPasswordVerify(ForgotPasswordVerifyRequest req) {
        otpService.verify(req.getMobileNumber(), req.getOtp(), Otp.Purpose.RESET_PASSWORD);

        User user = userRepository.findByMobileNumber(req.getMobileNumber())
                .orElseThrow(() -> new ResourceNotFoundException("No account found for this mobile number"));

        String token = TokenGenerator.generate();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getId())
                .expiryTime(LocalDateTime.now().plusMinutes(RESET_TOKEN_TTL_MINUTES))
                .used(false)
                .build();
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset token issued");
        return new PasswordResetTokenResponse(token, RESET_TOKEN_TTL_MINUTES * 60);
    }

    // ============================================================
    // Forgot password - step 3: reset password
    // ============================================================
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(req.getResetToken())
                .orElseThrow(() -> new InvalidResetTokenException("Invalid password reset token"));

        if (resetToken.isUsed()) {
            throw new InvalidResetTokenException("This password reset token has already been used");
        }
        if (LocalDateTime.now().isAfter(resetToken.getExpiryTime())) {
            throw new InvalidResetTokenException("Password reset token has expired. Please start again.");
        }
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new ApiException("New password and confirm password do not match");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset completed successfully");
    }

    // ============================================================
    // Resend OTP
    // ============================================================
    public void resendOtp(ResendOtpRequest req) {
        Otp.Purpose purpose;
        try {
            purpose = Otp.Purpose.valueOf(req.getPurpose().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException("Purpose must be REGISTER or RESET_PASSWORD");
        }
        otpService.resend(req.getMobileNumber(), purpose);
    }
}
