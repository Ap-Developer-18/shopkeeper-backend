package com.shopkeeper.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PasswordResetTokenResponse {
    private String resetToken;
    private int expiresInSeconds;
}
