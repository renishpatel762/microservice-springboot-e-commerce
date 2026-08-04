package com.ecommerce.order.exception;

public class ProductServiceUnavailableException extends RuntimeException {

    public ProductServiceUnavailableException(final String message) {
        super(message);
    }

    public ProductServiceUnavailableException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
