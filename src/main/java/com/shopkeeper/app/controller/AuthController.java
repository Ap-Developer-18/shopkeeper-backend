package com.shopkeeper.app.controller;

import com.shopkeeper.app.dto.*;
import com.shopkeeper.app.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, login, and password reset")
public class AuthController {

    private final AuthService authService;

    // ===================== Registration flow =====================

    @PostMapping("/register/initiate")
    public ResponseEntity<ApiResponse<RegisterInitiateResponse>> initiateRegistration(
            @Valid @RequestBody RegisterInitiateRequest req) {
        RegisterInitiateResponse data = authService.initiateRegistration(req);
        return ResponseEntity.ok(ApiResponse.success("OTP sent to your mobile number", data));
    }

    @PostMapping("/register/verify-otp")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyRegistrationOtp(
            @Valid @RequestBody VerifyOtpRequest req) {
        VerifyOtpResponse data = authService.verifyRegistrationOtp(req);
        return ResponseEntity.ok(ApiResponse.success("Mobile number verified", data));
    }

    @PostMapping("/register/complete")
    public ResponseEntity<ApiResponse<LoginResponse>> completeRegistration(
            @Valid @RequestBody CompleteRegistrationRequest req) {
        LoginResponse data = authService.completeRegistration(req);
        return ResponseEntity.ok(ApiResponse.success("Registration completed successfully", data));
    }

    // ===================== Login =====================

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse data = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success("Login successful", data));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest req) {
        LoginResponse data = authService.refreshAccessToken(req.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", data));
    }

    // ===================== Forgot password flow =====================

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPasswordInitiate(req);
        return ResponseEntity.ok(ApiResponse.success("OTP sent to your mobile number"));
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<ApiResponse<PasswordResetTokenResponse>> forgotPasswordVerify(
            @Valid @RequestBody ForgotPasswordVerifyRequest req) {
        PasswordResetTokenResponse data = authService.forgotPasswordVerify(req);
        return ResponseEntity.ok(ApiResponse.success("OTP verified", data));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }

    // ===================== Resend OTP =====================

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Object>> resendOtp(@Valid @RequestBody ResendOtpRequest req) {
        authService.resendOtp(req);
        return ResponseEntity.ok(ApiResponse.success("A new OTP has been sent"));
    }
}
