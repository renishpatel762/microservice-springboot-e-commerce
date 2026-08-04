package com.ecommerce.order.exception;

public class OrderCancellationException extends RuntimeException {

    public OrderCancellationException(final String message) {
        super(message);
    }
}
