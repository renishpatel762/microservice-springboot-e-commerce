package com.ecommerce.auth.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        OffsetDateTime timestamp,
        List<String> details
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, message, path, OffsetDateTime.now(), List.of());
    }

    public ErrorResponse(int status, String error, String message, String path, List<String> details) {
        this(status, error, message, path, OffsetDateTime.now(), details);
    }
}
