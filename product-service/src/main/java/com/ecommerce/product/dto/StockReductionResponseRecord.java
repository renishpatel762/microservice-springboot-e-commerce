package com.ecommerce.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Response returned after stock has been successfully reduced, providing
 * product snapshots (name and price) for Order Service.
 */
@Schema(description = "Response summary following stock reduction")
public record StockReductionResponseRecord(

    @Schema(description = "Product ID", example = "1")
    Long productId,

    @Schema(description = "Product name at purchase time", example = "Logitech MX Master 3S")
    String name,

    @Schema(description = "Unit price at purchase time", example = "99.99")
    BigDecimal price,

    @Schema(description = "Remaining stock after deduction", example = "148")
    Integer remainingStock
) {}
