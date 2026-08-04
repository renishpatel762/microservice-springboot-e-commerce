package com.ecommerce.order.dto;

import java.math.BigDecimal;

/**
 * Product detail representation received from Product Service.
 */
public record ProductDto(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    String category
) {}
