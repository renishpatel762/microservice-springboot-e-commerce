package com.ecommerce.order.controller;

import com.ecommerce.order.dto.ErrorResponseRecord;
import com.ecommerce.order.dto.OrderCreateRequestRecord;
import com.ecommerce.order.dto.OrderResponseRecord;
import com.ecommerce.order.dto.OrderSearchCriteria;
import com.ecommerce.order.dto.PagedResponseRecord;
import com.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management API", description = "Endpoints for placing, querying, and cancelling customer orders.")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Place a new order", description = "Validates items with Product Service, reduces stock, and creates a confirmed order.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload or insufficient stock",
                     content = @Content(schema = @Schema(implementation = ErrorResponseRecord.class))),
        @ApiResponse(responseCode = "404", description = "Requested product not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseRecord.class))),
        @ApiResponse(responseCode = "503", description = "Product Service unavailable",
                     content = @Content(schema = @Schema(implementation = ErrorResponseRecord.class)))
    })
    @PostMapping
    public ResponseEntity<OrderResponseRecord> createOrder(
            @Valid @RequestBody final OrderCreateRequestRecord requestRecord) {
        log.info("REST Request to create order for customer: {}", requestRecord.customerEmail());
        OrderResponseRecord response = orderService.createOrder(requestRecord);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get order by ID", description = "Retrieves details of a specific order by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order details found"),
        @ApiResponse(responseCode = "404", description = "Order not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseRecord.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseRecord> getOrderById(@PathVariable final Long id) {
        log.debug("REST Request to get order by ID: {}", id);
        OrderResponseRecord response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel an order", description = "Updates order status to CANCELLED.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order successfully cancelled"),
        @ApiResponse(responseCode = "400", description = "Order already cancelled",
                     content = @Content(schema = @Schema(implementation = ErrorResponseRecord.class))),
        @ApiResponse(responseCode = "404", description = "Order not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseRecord.class)))
    })
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponseRecord> cancelOrder(@PathVariable final Long id) {
        log.info("REST Request to cancel order ID: {}", id);
        OrderResponseRecord response = orderService.cancelOrder(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "List & Filter orders", description = "Returns a paginated list of orders matching filter criteria.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Paginated list of orders")
    })
    @GetMapping
    public ResponseEntity<PagedResponseRecord<OrderResponseRecord>> listOrders(
            @ModelAttribute final OrderSearchCriteria criteria) {
        log.debug("REST Request to list orders with criteria: {}", criteria);
        PagedResponseRecord<OrderResponseRecord> pageResponse = orderService.listOrders(criteria);
        return ResponseEntity.ok(pageResponse);
    }
}
