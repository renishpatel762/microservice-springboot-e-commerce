package com.ecommerce.order.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(final Long productId) {
        super("Requested product with ID " + productId + " does not exist in Product Service");
    }

    public ProductNotFoundException(final String message) {
        super(message);
    }
}
