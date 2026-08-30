package com.shopkeeper.app.service;

import com.shopkeeper.app.entity.User;
import com.shopkeeper.app.exception.ApiException;
import com.shopkeeper.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException("Authenticated user not found"));
    }
}
