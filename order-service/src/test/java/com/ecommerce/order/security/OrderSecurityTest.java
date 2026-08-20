package com.ecommerce.order.security;

import com.ecommerce.order.config.SecurityConfig;
import com.ecommerce.order.controller.OrderController;
import com.ecommerce.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class OrderSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("Unauthenticated request to GET /api/v1/orders/1 - Should return 401 Unauthorized")
    void getOrder_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/orders/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authenticated request with valid Bearer token - Should be permitted")
    void getOrder_Authenticated_Success() throws Exception {
        when(jwtService.isTokenValid("valid-order-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-order-token")).thenReturn("user@example.com");
        when(jwtService.extractRole("valid-order-token")).thenReturn("ROLE_USER");

        mockMvc.perform(get("/api/v1/orders/1")
                        .header("Authorization", "Bearer valid-order-token"))
                .andExpect(status().isOk());
    }
}
