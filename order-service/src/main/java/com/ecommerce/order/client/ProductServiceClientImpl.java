package com.ecommerce.order.client;

import com.ecommerce.order.dto.ErrorResponseRecord;
import com.ecommerce.order.dto.ProductDto;
import com.ecommerce.order.dto.StockReductionDto;
import com.ecommerce.order.exception.InsufficientStockException;
import com.ecommerce.order.exception.ProductNotFoundException;
import com.ecommerce.order.exception.ProductServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/**
 * ===================================================================================
 * REST INTER-SERVICE CLIENT DESIGN PATTERN:
 * ===================================================================================
 * 1. WHY IT EXISTS:
 *    Centralizes REST communication with Product Service. Encapsulates HTTP details, URL pathing,
 *    JSON serialization, status mapping, and exception translation into clean domain methods.
 *
 * 2. ERROR TRANSLATION (.onStatus):
 *    Intercepts HTTP status codes returned by Product Service and translates downstream error JSON payloads
 *    into application-specific domain exceptions (`ProductNotFoundException`, `InsufficientStockException`, `ProductServiceUnavailableException`).
 * ===================================================================================
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClientImpl implements ProductServiceClient {

    private final WebClient productServiceWebClient;

    @Override
    public ProductDto getProductById(final Long productId) {
        log.info("Calling Product Service via WebClient: GET /api/v1/products/{}", productId);

        try {
            return productServiceWebClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                            return Mono.error(new ProductNotFoundException(productId));
                        }
                        return response.bodyToMono(ErrorResponseRecord.class)
                                .flatMap(err -> Mono.error(new ProductNotFoundException("Product validation failed: " + err.message())));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            Mono.error(new ProductServiceUnavailableException("Product Service is currently unavailable (HTTP " + response.statusCode() + ")")))
                    .bodyToMono(ProductDto.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClient error while fetching product ID {}: {}", productId, ex.getMessage());
            throw new ProductServiceUnavailableException("Failed to communicate with Product Service", ex);
        } catch (Exception ex) {
            if (ex instanceof ProductNotFoundException || ex instanceof ProductServiceUnavailableException) {
                throw ex;
            }
            log.error("Unexpected error during product fetch ID {}: {}", productId, ex.getMessage());
            throw new ProductServiceUnavailableException("Product Service call failed", ex);
        }
    }

    @Override
    public StockReductionDto.Response reduceStock(final Long productId, final Integer quantity) {
        log.info("Calling Product Service via WebClient: POST /api/v1/products/reduce-stock for product ID: {}, quantity: {}", productId, quantity);

        StockReductionDto.Request request = new StockReductionDto.Request(productId, quantity);

        try {
            return productServiceWebClient.post()
                    .uri("/api/v1/products/reduce-stock")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                            return Mono.error(new ProductNotFoundException(productId));
                        }
                        return response.bodyToMono(ErrorResponseRecord.class)
                                .flatMap(err -> {
                                    if ("INSUFFICIENT_STOCK".equals(err.error())) {
                                        return Mono.error(new InsufficientStockException(err.message()));
                                    }
                                    return Mono.error(new InsufficientStockException("Stock reduction failed: " + err.message()));
                                });
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response ->
                            Mono.error(new ProductServiceUnavailableException("Product Service error during stock reduction (HTTP " + response.statusCode() + ")")))
                    .bodyToMono(StockReductionDto.Response.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClient error during stock reduction for product ID {}: {}", productId, ex.getMessage());
            throw new ProductServiceUnavailableException("Failed to reduce stock in Product Service", ex);
        } catch (Exception ex) {
            if (ex instanceof ProductNotFoundException || ex instanceof InsufficientStockException || ex instanceof ProductServiceUnavailableException) {
                throw ex;
            }
            log.error("Unexpected error during stock reduction for product ID {}: {}", productId, ex.getMessage());
            throw new ProductServiceUnavailableException("Stock reduction call failed", ex);
        }
    }
}
