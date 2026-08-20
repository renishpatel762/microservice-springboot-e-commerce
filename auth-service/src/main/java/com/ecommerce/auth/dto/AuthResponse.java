package com.ecommerce.auth.dto;

import com.ecommerce.auth.enums.Role;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String email,
        String fullName,
        Role role
) {
    public AuthResponse(String accessToken, long expiresIn, Long userId, String email, String fullName, Role role) {
        this(accessToken, "Bearer", expiresIn, userId, email, fullName, role);
    }
}
