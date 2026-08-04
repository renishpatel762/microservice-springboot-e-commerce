package com.ecommerce.product.exception;

import com.ecommerce.product.dto.ErrorResponseRecord;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * ===================================================================================
 * GLOBAL EXCEPTION HANDLING PATTERN:
 * ===================================================================================
 * 1. WHY IT EXISTS:
 *    Centralizes error processing across all REST controllers using AOP (Aspect-Oriented Programming).
 *    Interceptor intercepts exceptions thrown anywhere in HTTP request processing.
 *
 * 2. WHY IT IS BETTER THAN ALTERNATIVES:
 *    Without @RestControllerAdvice, developers wrap every single controller endpoint in duplicate
 *    try-catch blocks or leak raw stack traces and internal DB exception messages to clients.
 *
 * 3. WHEN IT SHOULD BE USED:
 *    In every production REST application to guarantee a unified error envelope format and correct
 *    HTTP status codes (400, 404, 409, 500).
 *
 * 4. COMMON MISTAKES DEVELOPERS MAKE:
 *    - Catching generic Exception without logging stack traces, hiding server bugs.
 *    - Returning HTTP 200 OK with an error status inside the JSON body ("200 OK Anti-pattern").
 *    - Leaking SQL syntax or stack traces in error messages to clients (security risk).
 * ===================================================================================
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponseRecord> handleProductNotFound(
            final ProductNotFoundException ex,
            final HttpServletRequest request) {

        log.warn("Resource not found exception on path {}: {}", request.getRequestURI(), ex.getMessage());

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

        log.warn("Insufficient stock exception on path {}: {}", request.getRequestURI(), ex.getMessage());

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseRecord> handleIllegalArgument(
            final IllegalArgumentException ex,
            final HttpServletRequest request) {

        log.warn("Illegal argument exception on path {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponseRecord error = new ErrorResponseRecord(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_ARGUMENT",
                ex.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseRecord> handleGenericException(
            final Exception ex,
            final HttpServletRequest request) {

        log.error("Unhandled internal server exception on path {}", request.getRequestURI(), ex);

        ErrorResponseRecord error = new ErrorResponseRecord(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "An unexpected internal error occurred. Please contact system administrator.",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
