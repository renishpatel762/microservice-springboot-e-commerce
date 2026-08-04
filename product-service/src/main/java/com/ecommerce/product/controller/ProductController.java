package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ErrorResponseRecord;
import com.ecommerce.product.dto.PagedResponseRecord;
import com.ecommerce.product.dto.ProductRequestRecord;
import com.ecommerce.product.dto.ProductResponseRecord;
import com.ecommerce.product.dto.ProductSearchCriteria;
import com.ecommerce.product.dto.StockReductionRequestRecord;
import com.ecommerce.product.dto.StockReductionResponseRecord;
import com.ecommerce.product.service.ProductService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ===================================================================================
 * REST CONTROLLER ARCHITECTURE & PRODUCTION STANDARDS:
 * ===================================================================================
 * 1. WHY REST CONTROLLERS ARE THIN:
 *    Controllers should contain ZERO business logic. Their sole responsibility is HTTP request parsing,
 *    triggering `@Valid` Bean validation, delegating execution to domain services, and setting HTTP response headers/status.
 *
 * 2. API VERSIONING (/api/v1/products):
 *    Prefixing endpoints with explicit API versions allows introducing breaking API changes in future v2 versions
 *    without breaking existing client mobile/frontend installations.
 *
 * 3. HTTP STATUS CODE ACCURACY:
 *    - POST creation endpoints MUST return HTTP 201 Created.
 *    - DELETE endpoints MUST return HTTP 204 No Content.
 *    - Successful GET/PUT queries return HTTP 200 OK.
 * ===================================================================================
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Management API", description = "Endpoints for creating, searching, updating, and managing product catalog items.")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Create a new product", description = "Stores a new product entry in the catalog.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid request payload validation failure",
                     content = @Content(schema = @Schema(implementation = ErrorResponseRecord.class)))
    })
    @PostMapping
    public ResponseEntity<ProductResponseRecord> createProduct(
            @Valid @RequestBody final ProductRequestRecord requestRecord) {
        log.info("REST Request to create product: {}", requestRecord.name());
        ProductResponseRecord response = productService.createProduct(requestRecord);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Search & Filter products", description = "Returns a paginated list of products matching search criteria.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Paginated product search results")
    })
    @GetMapping
    public ResponseEntity<PagedResponseRecord<ProductResponseRecord>> searchProducts(
            @ModelAttribute final ProductSearchCriteria criteria) {
        log.debug("REST Request to search products with criteria: {}", criteria);
        PagedResponseRecord<ProductResponseRecord> pageResponse = productService.searchProducts(criteria);
        return ResponseEntity.ok(pageResponse);
    }

    @Operation(summary = "Get product by ID", description = "Retrieves details of a specific product.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product details found"),
        @ApiResponse(responseCode = "404", description = "Product not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseRecord.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseRecord> getProductById(@PathVariable final Long id) {
        log.debug("REST Request to get product by ID: {}", id);
        ProductResponseRecord response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update an existing product", description = "Updates details of a product specified by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product successfully updated"),
        @ApiResponse(responseCode = "400", description = "Validation error",
                     content = @Content(schema = @Schema(implementation = ErrorResponseRecord.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseRecord.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseRecord> updateProduct(
            @PathVariable final Long id,
            @Valid @RequestBody final ProductRequestRecord requestRecord) {
        log.info("REST Request to update product ID: {}", id);
        ProductResponseRecord response = productService.updateProduct(id, requestRecord);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a product", description = "Removes a product from the database catalog by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Product not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseRecord.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable final Long id) {
        log.info("REST Request to delete product ID: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reduce product stock", description = "Endpoint invoked by Order Service during checkout to deduct stock.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock successfully reduced"),
        @ApiResponse(responseCode = "400", description = "Insufficient stock or invalid request",
                     content = @Content(schema = @Schema(implementation = ErrorResponseRecord.class))),
        @ApiResponse(responseCode = "404", description = "Product not found",
                     content = @Content(schema = @Schema(implementation = ErrorResponseRecord.class)))
    })
    @PostMapping("/reduce-stock")
    public ResponseEntity<StockReductionResponseRecord> reduceStock(
            @Valid @RequestBody final StockReductionRequestRecord requestRecord) {
        log.info("REST Request to reduce stock for product ID: {}", requestRecord.productId());
        StockReductionResponseRecord response = productService.reduceStock(requestRecord);
        return ResponseEntity.ok(response);
    }
}
