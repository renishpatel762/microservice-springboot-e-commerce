package com.ecommerce.product.exception;

/**
 * Thrown when requested stock reduction quantity exceeds currently available inventory.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(final Long productId, final int requested, final int available) {
        super(String.format("Insufficient stock for product ID %d: requested %d, available %d", productId, requested, available));
    }

    public InsufficientStockException(final String message) {
        super(message);
    }
}
