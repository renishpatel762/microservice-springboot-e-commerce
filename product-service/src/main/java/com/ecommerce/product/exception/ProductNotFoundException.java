package com.ecommerce.product.exception;

/**
 * Thrown when a requested Product entity does not exist in the database.
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(final Long id) {
        super("Product with ID " + id + " was not found");
    }

    public ProductNotFoundException(final String message) {
        super(message);
    }
}
