package com.shopkeeper.app.dto;

import com.shopkeeper.app.validation.ValidPassword;
import com.shopkeeper.app.validation.ValidUsername;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompleteRegistrationRequest {
    @NotBlank(message = "Registration token is required")
    private String registrationToken;

    @ValidUsername
    private String username;

    @ValidPassword
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
