package com.ecommerce.order.exception;

import com.ecommerce.order.dto.ErrorResponseRecord;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponseRecord> handleOrderNotFound(
            final OrderNotFoundException ex,
            final HttpServletRequest request) {

        log.warn("Order not found on path {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponseRecord error = new ErrorResponseRecord(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "ORDER_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponseRecord> handleProductNotFound(
            final ProductNotFoundException ex,
            final HttpServletRequest request) {

        log.warn("Product validation failed on path {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponseRecord error = new ErrorResponseRecord(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "PRODUCT_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponseRecord> handleInsufficientStock(
            final InsufficientStockException ex,
            final HttpServletRequest request) {

        log.warn("Stock insufficiency on path {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponseRecord error = new ErrorResponseRecord(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "INSUFFICIENT_STOCK",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(OrderCancellationException.class)
    public ResponseEntity<ErrorResponseRecord> handleOrderCancellation(
            final OrderCancellationException ex,
            final HttpServletRequest request) {

        log.warn("Order cancellation error on path {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponseRecord error = new ErrorResponseRecord(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "ORDER_CANCELLATION_FAILED",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ProductServiceUnavailableException.class)
    public ResponseEntity<ErrorResponseRecord> handleProductServiceUnavailable(
            final ProductServiceUnavailableException ex,
            final HttpServletRequest request) {

        log.error("Downstream Product Service failure on path {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponseRecord error = new ErrorResponseRecord(
                Instant.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "PRODUCT_SERVICE_UNAVAILABLE",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseRecord> handleValidationExceptions(
            final MethodArgumentNotValidException ex,
            final HttpServletRequest request) {

        log.warn("Validation error on path {}: {} field errors", request.getRequestURI(), ex.getBindingResult().getFieldErrorCount());

        List<ErrorResponseRecord.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ErrorResponseRecord.ValidationError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()))
                .toList();

        ErrorResponseRecord error = new ErrorResponseRecord(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "Input validation failed. Please check validationErrors field for details.",
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseRecord> handleGenericException(
            final Exception ex,
            final HttpServletRequest request) {

        log.error("Unhandled internal server error on path {}", request.getRequestURI(), ex);

        ErrorResponseRecord error = new ErrorResponseRecord(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected internal error occurred. Please contact administrator.",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
