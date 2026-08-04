package com.ecommerce.order.dto;

import com.ecommerce.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Filter criteria DTO for querying orders.
 */
@Schema(description = "Query criteria for searching orders")
public record OrderSearchCriteria(

    @Schema(description = "Filter by order status", example = "CONFIRMED")
    OrderStatus status,

    @Schema(description = "Filter by customer email address", example = "john.doe@example.com")
    String customerEmail,

    @Schema(description = "Zero-indexed page number", example = "0", defaultValue = "0")
    Integer page,

    @Schema(description = "Number of items per page", example = "10", defaultValue = "10")
    Integer size,

    @Schema(description = "Field name to sort by", example = "createdAt", defaultValue = "createdAt")
    String sortBy,

    @Schema(description = "Sort direction (ASC or DESC)", example = "DESC", defaultValue = "DESC")
    String sortDir
) {
    public OrderSearchCriteria {
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size <= 0) {
            size = 10;
        }
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "createdAt";
        }
        if (sortDir == null || (!sortDir.equalsIgnoreCase("ASC") && !sortDir.equalsIgnoreCase("DESC"))) {
            sortDir = "DESC";
        }
    }
}
