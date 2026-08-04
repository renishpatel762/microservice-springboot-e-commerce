package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable API response DTO representing product entity state.
 */
@Schema(description = "Response details of a product")
public record ProductResponseRecord(

    @Schema(description = "Unique identifier of the product", example = "1")
    Long id,

    @Schema(description = "Product name", example = "Logitech MX Master 3S")
    String name,

    @Schema(description = "Product description", example = "Ergonomic wireless performance mouse")
    String description,

    @Schema(description = "Unit price", example = "99.99")
    BigDecimal price,

    @Schema(description = "Available inventory stock", example = "150")
    Integer stock,

    @Schema(description = "Product category", example = "Electronics")
    String category,

    @Schema(description = "Creation timestamp")
    Instant createdAt,

    @Schema(description = "Last update timestamp")
    Instant updatedAt
) {}
