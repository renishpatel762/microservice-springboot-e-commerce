package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderCreateRequestRecord;
import com.ecommerce.order.dto.OrderItemRequestRecord;
import com.ecommerce.order.dto.OrderResponseRecord;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.service.OrderService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /api/v1/orders - Should return 201 Created and response DTO")
    void createOrder_Success() throws Exception {
        OrderCreateRequestRecord requestRecord = new OrderCreateRequestRecord(
                "Alice Smith",
                "alice@example.com",
                List.of(new OrderItemRequestRecord(1L, 2))
        );

        OrderResponseRecord responseRecord = new OrderResponseRecord(
                101L,
                "Alice Smith",
                "alice@example.com",
                OrderStatus.CONFIRMED,
                new BigDecimal("199.98"),
                Instant.now(),
                List.of()
        );

        when(orderService.createOrder(any(OrderCreateRequestRecord.class))).thenReturn(responseRecord);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestRecord)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} - Should return 404 Not Found when order does not exist")
    void getOrderById_NotFound() throws Exception {
        when(orderService.getOrderById(999L)).thenThrow(new OrderNotFoundException(999L));

        mockMvc.perform(get("/api/v1/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Order with ID 999 was not found"));
    }
}
