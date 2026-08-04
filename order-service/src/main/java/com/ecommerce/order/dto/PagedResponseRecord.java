package com.ecommerce.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Standard pagination response envelope for order endpoints.
 */
@Schema(description = "Generic paginated response envelope")
public record PagedResponseRecord<T>(

    @Schema(description = "List of record items for current page")
    List<T> content,

    @Schema(description = "Current page index (0-indexed)", example = "0")
    int pageNo,

    @Schema(description = "Page size limit", example = "10")
    int pageSize,

    @Schema(description = "Total matched records count across all pages", example = "15")
    long totalElements,

    @Schema(description = "Total calculated pages", example = "2")
    int totalPages,

    @Schema(description = "Indicates if this is the final page", example = "true")
    boolean last
) {}
