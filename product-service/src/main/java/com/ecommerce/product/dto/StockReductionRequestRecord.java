package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request payload for reducing product stock inventory.
 */
@Schema(description = "Stock reduction request for an item")
public record StockReductionRequestRecord(

    @Schema(description = "Product ID to reduce stock for", example = "1")
    @NotNull(message = "Product ID is required")
    Long productId,

    @Schema(description = "Quantity to reduce", example = "2")
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    Integer quantity
) {}
