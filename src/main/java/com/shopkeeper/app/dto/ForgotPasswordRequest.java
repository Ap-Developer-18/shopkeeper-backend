package com.shopkeeper.app.dto;

import com.shopkeeper.app.validation.ValidMobileNumber;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @ValidMobileNumber
    private String mobileNumber;
}
