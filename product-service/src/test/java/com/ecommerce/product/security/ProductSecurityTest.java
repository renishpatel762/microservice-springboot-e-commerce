package com.ecommerce.product.security;

import com.ecommerce.product.config.SecurityConfig;
import com.ecommerce.product.controller.ProductController;
import com.ecommerce.product.service.ProductService;
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

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ProductSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("Unauthenticated request to GET /api/v1/products - Should return 401 Unauthorized")
    void getProducts_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Authenticated request with valid Bearer token - Should be permitted")
    void getProducts_Authenticated_Success() throws Exception {
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-token")).thenReturn("user@example.com");
        when(jwtService.extractRole("valid-token")).thenReturn("ROLE_USER");

        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }
}
