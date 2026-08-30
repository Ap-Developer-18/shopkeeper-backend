package com.shopkeeper.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegisterInitiateResponse {
    private String mobileNumber;
    private int otpExpirySeconds;
}
