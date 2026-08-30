package com.shopkeeper.app.dto;

import com.shopkeeper.app.validation.ValidMobileNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendOtpRequest {
    @ValidMobileNumber
    private String mobileNumber;

    // "REGISTER" or "RESET_PASSWORD"
    @NotBlank(message = "Purpose is required")
    private String purpose;
}
