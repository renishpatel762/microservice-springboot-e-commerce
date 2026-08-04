package com.ecommerce.order;

import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.dto.OrderCreateRequestRecord;
import com.ecommerce.order.dto.OrderItemRequestRecord;
import com.ecommerce.order.dto.OrderResponseRecord;
import com.ecommerce.order.dto.ProductDto;
import com.ecommerce.order.dto.StockReductionDto;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnabledIf("isDockerAvailable")
class OrderIntegrationTest {

    static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Throwable t) {
            return false;
        }
    }

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private ProductServiceClient productServiceClient;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration Test: Complete Order creation flow with Mocked WebClient and Testcontainers PostgreSQL")
    void testOrderCreationIntegration() {
        ProductDto mockProduct = new ProductDto(
                5L, "Wireless Mouse", "Ergonomic", new BigDecimal("49.99"), 100, "Electronics"
        );
        StockReductionDto.Response mockReduction = new StockReductionDto.Response(
                5L, "Wireless Mouse", new BigDecimal("49.99"), 98
        );

        when(productServiceClient.getProductById(5L)).thenReturn(mockProduct);
        when(productServiceClient.reduceStock(eq(5L), eq(2))).thenReturn(mockReduction);

        OrderCreateRequestRecord createRequest = new OrderCreateRequestRecord(
                "Bob Johnson",
                "bob.johnson@example.com",
                List.of(new OrderItemRequestRecord(5L, 2))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrderCreateRequestRecord> requestEntity = new HttpEntity<>(createRequest, headers);

        ResponseEntity<OrderResponseRecord> responseEntity = restTemplate.postForEntity(
                "/api/v1/orders", requestEntity, OrderResponseRecord.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().id()).isNotNull();
        assertThat(responseEntity.getBody().status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(responseEntity.getBody().totalAmount()).isEqualTo(new BigDecimal("99.98"));
    }
}
