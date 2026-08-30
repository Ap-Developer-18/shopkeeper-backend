package com.shopkeeper.app.dto;

import com.shopkeeper.app.validation.ValidMobileNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterInitiateRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @ValidMobileNumber
    private String mobileNumber;
}
