package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * ===================================================================================
 * IMMUTABLE DTO RECORD & BEAN VALIDATION:
 * ===================================================================================
 * 1. WHY IT EXISTS:
 *    Java Records act as immutable data containers. Spring Web deserializes JSON request
 *    bodies directly into record components, enforcing thread-safe, unmodifiable DTOs.
 *
 * 2. WHY IT IS BETTER THAN ALTERNATIVES:
 *    Traditional mutable JavaBean classes allow accidental mutation during business processing.
 *    Records enforce state immutability at the language syntax level.
 *
 * 3. BEAN VALIDATION (@Valid):
 *    Declarative constraint annotations validate client input before service methods run,
 *    failing fast with HTTP 400 Bad Request if rules are violated.
 * ===================================================================================
 */
@Schema(description = "Payload for creating or updating a product")
public record ProductRequestRecord(

    @Schema(description = "Product title/name", example = "Logitech MX Master 3S")
    @NotBlank(message = "Product name must not be blank")
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    String name,

    @Schema(description = "Detailed product description", example = "Ergonomic wireless performance mouse")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    String description,

    @Schema(description = "Unit price in USD", example = "99.99")
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    BigDecimal price,

    @Schema(description = "Initial stock quantity", example = "150")
    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock cannot be negative")
    Integer stock,

    @Schema(description = "Product category identifier", example = "Electronics")
    @NotBlank(message = "Category must not be blank")
    @Size(min = 2, max = 100, message = "Category must be between 2 and 100 characters")
    String category
) {}
