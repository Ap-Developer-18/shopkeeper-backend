package com.shopkeeper.app.mapper;

import com.shopkeeper.app.dto.UserResponse;
import com.shopkeeper.app.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .companyName(user.getCompanyName())
                .mobileNumber(user.getMobileNumber())
                .username(user.getUsername())
                .phoneVerified(user.isPhoneVerified())
                .status(user.getStatus().name())
                .build();
    }
}
