package com.ecommerce.order.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(final String message) {
        super(message);
    }
}
