package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Standard pagination response wrapper preventing leakage of Spring Data Page internal structures.
 */
@Schema(description = "Generic paginated response envelope")
public record PagedResponseRecord<T>(

    @Schema(description = "List of record items for the current page")
    List<T> content,

    @Schema(description = "Current page index (0-indexed)", example = "0")
    int pageNo,

    @Schema(description = "Page size limit", example = "10")
    int pageSize,

    @Schema(description = "Total matched records count across all pages", example = "42")
    long totalElements,

    @Schema(description = "Total calculated pages", example = "5")
    int totalPages,

    @Schema(description = "Indicates if this is the final page", example = "false")
    boolean last
) {}
