package com.ecommerce.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Payload for placing a new customer order.
 */
@Schema(description = "Request payload for creating a new order")
public record OrderCreateRequestRecord(

    @Schema(description = "Full customer name", example = "John Doe")
    @NotBlank(message = "Customer name must not be blank")
    @Size(min = 2, max = 255, message = "Customer name must be between 2 and 255 characters")
    String customerName,

    @Schema(description = "Customer email address", example = "john.doe@example.com")
    @NotBlank(message = "Customer email must not be blank")
    @Email(message = "Customer email must be valid")
    String customerEmail,

    @Schema(description = "List of ordered items")
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    List<OrderItemRequestRecord> items
) {}
