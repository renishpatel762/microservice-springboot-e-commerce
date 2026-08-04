package com.ecommerce.order.dto;

import java.math.BigDecimal;

public class StockReductionDto {

    public record Request(
        Long productId,
        Integer quantity
    ) {}

    public record Response(
        Long productId,
        String name,
        BigDecimal price,
        Integer remainingStock
    ) {}
}
