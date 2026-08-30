package com.shopkeeper.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyOtpResponse {
    private String registrationToken;
    private int expiresInSeconds;
}
