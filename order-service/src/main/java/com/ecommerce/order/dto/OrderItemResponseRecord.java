package com.ecommerce.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Response record item snapshot inside an order response payload.
 */
@Schema(description = "Order line item response detail")
public record OrderItemResponseRecord(

    @Schema(description = "Order item ID", example = "10")
    Long id,

    @Schema(description = "Product ID", example = "1")
    Long productId,

    @Schema(description = "Snapshot of product name at purchase", example = "Logitech MX Master 3S")
    String productName,

    @Schema(description = "Snapshot unit price at purchase", example = "99.99")
    BigDecimal priceAtPurchase,

    @Schema(description = "Purchased quantity", example = "2")
    Integer quantity,

    @Schema(description = "Calculated total item price (priceAtPurchase * quantity)", example = "199.98")
    BigDecimal totalPrice
) {}
