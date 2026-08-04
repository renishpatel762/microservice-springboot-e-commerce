package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Standardized enterprise API error response object.
 */
@Schema(description = "Standardized error response payload")
public record ErrorResponseRecord(

    @Schema(description = "Timestamp when error occurred", example = "2026-08-03T22:15:00Z")
    Instant timestamp,

    @Schema(description = "HTTP Status code number", example = "404")
    int status,

    @Schema(description = "Machine-readable error code", example = "PRODUCT_NOT_FOUND")
    String error,

    @Schema(description = "Human-readable error explanation", example = "Product with ID 10 was not found")
    String message,

    @Schema(description = "Request URI path", example = "/api/v1/products/10")
    String path,

    @Schema(description = "Detailed field validation errors (if applicable)")
    List<ValidationError> validationErrors
) {
    @Schema(description = "Specific field validation detail")
    public record ValidationError(
        String field,
        String message
    ) {}
}
