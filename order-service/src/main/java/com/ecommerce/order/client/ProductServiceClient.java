package com.ecommerce.order.client;

import com.ecommerce.order.dto.ProductDto;
import com.ecommerce.order.dto.StockReductionDto;

/**
 * REST Client contract for inter-service communication with Product Service.
 */
public interface ProductServiceClient {

    ProductDto getProductById(Long productId);

    StockReductionDto.Response reduceStock(Long productId, Integer quantity);
}
