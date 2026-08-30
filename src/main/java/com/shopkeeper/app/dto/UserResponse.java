package com.shopkeeper.app.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String companyName;
    private String mobileNumber;
    private String username;
    private boolean phoneVerified;
    private String status;
}
