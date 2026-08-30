package com.shopkeeper.app.dto;

import com.shopkeeper.app.validation.ValidMobileNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @ValidMobileNumber
    private String mobileNumber;

    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be exactly 6 digits")
    private String otp;
}
