package com.ecommerce.auth.dto;

import com.ecommerce.auth.enums.Role;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        Role role,
        OffsetDateTime createdAt
) {}
