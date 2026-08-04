package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductRequestRecord;
import com.ecommerce.product.dto.ProductResponseRecord;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @DisplayName("POST /api/v1/products - Should return 201 Created and response DTO")
    void createProduct_Success() throws Exception {
        ProductRequestRecord requestRecord = new ProductRequestRecord(
                "Gaming Laptop",
                "High performance laptop",
                new BigDecimal("1299.99"),
                20,
                "Computers"
        );

        ProductResponseRecord responseRecord = new ProductResponseRecord(
                1L,
                "Gaming Laptop",
                "High performance laptop",
                new BigDecimal("1299.99"),
                20,
                "Computers",
                Instant.now(),
                Instant.now()
        );

        when(productService.createProduct(any(ProductRequestRecord.class))).thenReturn(responseRecord);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestRecord)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Gaming Laptop"))
                .andExpect(jsonPath("$.price").value(1299.99));
    }

    @Test
    @DisplayName("POST /api/v1/products - Should return 400 Bad Request when payload is invalid")
    void createProduct_InvalidPayload_ReturnsBadRequest() throws Exception {
        ProductRequestRecord invalidRequest = new ProductRequestRecord(
                "", // Blank name
                "High performance laptop",
                new BigDecimal("-50.00"), // Negative price
                -5, // Negative stock
                "" // Blank category
        );

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - Should return 404 Not Found when product does not exist")
    void getProductById_NotFound() throws Exception {
        when(productService.getProductById(99L)).thenThrow(new ProductNotFoundException(99L));

        mockMvc.perform(get("/api/v1/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Product with ID 99 was not found"));
    }
}
