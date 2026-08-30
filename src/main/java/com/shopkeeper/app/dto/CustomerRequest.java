package com.shopkeeper.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRequest {
    @NotBlank private String name;
    @NotBlank private String phone;
    private String whatsappNumber;
    private String email;
    private String address;
}
