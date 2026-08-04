package com.ecommerce.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request item entry when placing an order.
 */
@Schema(description = "Item specification for placing an order")
public record OrderItemRequestRecord(

    @Schema(description = "Target Product ID", example = "1")
    @NotNull(message = "Product ID is required")
    Long productId,

    @Schema(description = "Purchasing quantity", example = "2")
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    Integer quantity
) {}
