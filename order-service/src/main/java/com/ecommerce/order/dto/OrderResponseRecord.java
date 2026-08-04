package com.ecommerce.order.dto;

import com.ecommerce.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Complete order response representation payload.
 */
@Schema(description = "Order response detail payload")
public record OrderResponseRecord(

    @Schema(description = "Order ID", example = "100")
    Long id,

    @Schema(description = "Customer name", example = "John Doe")
    String customerName,

    @Schema(description = "Customer email", example = "john.doe@example.com")
    String customerEmail,

    @Schema(description = "Order status", example = "CONFIRMED")
    OrderStatus status,

    @Schema(description = "Total order amount", example = "199.98")
    BigDecimal totalAmount,

    @Schema(description = "Creation timestamp")
    Instant createdAt,

    @Schema(description = "List of ordered items")
    List<OrderItemResponseRecord> items
) {}
