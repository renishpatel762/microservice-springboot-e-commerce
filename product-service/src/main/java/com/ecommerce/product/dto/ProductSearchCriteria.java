package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Filter criteria DTO for searching, sorting, and paginating products.
 */
@Schema(description = "Criteria parameters for searching and filtering products")
public record ProductSearchCriteria(

    @Schema(description = "Filter by category", example = "Electronics")
    String category,

    @Schema(description = "Filter by product name substring (case-insensitive)", example = "logitech")
    String name,

    @Schema(description = "Filter by minimum price", example = "10.00")
    BigDecimal minPrice,

    @Schema(description = "Filter by maximum price", example = "500.00")
    BigDecimal maxPrice,

    @Schema(description = "Zero-indexed page number", example = "0", defaultValue = "0")
    Integer page,

    @Schema(description = "Number of items per page", example = "10", defaultValue = "10")
    Integer size,

    @Schema(description = "Field name to sort by", example = "createdAt", defaultValue = "createdAt")
    String sortBy,

    @Schema(description = "Sort direction (ASC or DESC)", example = "DESC", defaultValue = "DESC")
    String sortDir
) {
    public ProductSearchCriteria {
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
