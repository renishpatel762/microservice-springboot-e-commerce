package com.ecommerce.order.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(final Long id) {
        super("Order with ID " + id + " was not found");
    }

    public OrderNotFoundException(final String message) {
        super(message);
    }
}
