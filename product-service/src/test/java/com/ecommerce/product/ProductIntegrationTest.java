package com.ecommerce.product;

import com.ecommerce.product.dto.ProductRequestRecord;
import com.ecommerce.product.dto.ProductResponseRecord;
import com.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test running product service against a real PostgreSQL instance containerized via Testcontainers.
 * Conditionally executes if a local Docker daemon is available.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnabledIf("isDockerAvailable")
class ProductIntegrationTest {

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
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration Test: Complete product lifecycle - Create, Read, Delete with Testcontainers PostgreSQL")
    void testProductLifecycleIntegration() {
        // 1. Create Product
        ProductRequestRecord createRequest = new ProductRequestRecord(
                "4K Monitor",
                "32 inch UHD display",
                new BigDecimal("499.99"),
                15,
                "Electronics"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProductRequestRecord> requestEntity = new HttpEntity<>(createRequest, headers);

        ResponseEntity<ProductResponseRecord> createResponse = restTemplate.postForEntity(
                "/api/v1/products", requestEntity, ProductResponseRecord.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        Long createdId = createResponse.getBody().id();
        assertThat(createdId).isNotNull();
        assertThat(createResponse.getBody().name()).isEqualTo("4K Monitor");

        // 2. Fetch Created Product by ID
        ResponseEntity<ProductResponseRecord> getResponse = restTemplate.getForEntity(
                "/api/v1/products/" + createdId, ProductResponseRecord.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().id()).isEqualTo(createdId);

        // 3. Delete Product
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/v1/products/" + createdId, HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 4. Confirm Deletion (Expect 404)
        ResponseEntity<String> getDeletedResponse = restTemplate.getForEntity(
                "/api/v1/products/" + createdId, String.class);

        assertThat(getDeletedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
