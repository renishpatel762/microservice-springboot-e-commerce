package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequestRecord;
import com.ecommerce.product.dto.ProductResponseRecord;
import com.ecommerce.product.dto.StockReductionRequestRecord;
import com.ecommerce.product.dto.StockReductionResponseRecord;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.exception.InsufficientStockException;
import com.ecommerce.product.exception.ProductNotFoundException;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for ProductServiceImpl testing business logic in isolation using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product sampleEntity;
    private ProductRequestRecord sampleRequest;
    private ProductResponseRecord sampleResponse;

    @BeforeEach
    void setUp() {
        sampleEntity = Product.builder()
                .id(1L)
                .name("Wireless Keyboard")
                .description("Mechanical keyboard")
                .price(new BigDecimal("79.99"))
                .stock(50)
                .category("Electronics")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        sampleRequest = new ProductRequestRecord(
                "Wireless Keyboard",
                "Mechanical keyboard",
                new BigDecimal("79.99"),
                50,
                "Electronics"
        );

        sampleResponse = new ProductResponseRecord(
                1L,
                "Wireless Keyboard",
                "Mechanical keyboard",
                new BigDecimal("79.99"),
                50,
                "Electronics",
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("Should successfully create product")
    void createProduct_Success() {
        when(productMapper.toEntity(sampleRequest)).thenReturn(sampleEntity);
        when(productRepository.save(sampleEntity)).thenReturn(sampleEntity);
        when(productMapper.toResponse(sampleEntity)).thenReturn(sampleResponse);

        ProductResponseRecord result = productService.createProduct(sampleRequest);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Wireless Keyboard");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should return product response when valid ID is provided")
    void getProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(productMapper.toResponse(sampleEntity)).thenReturn(sampleResponse);

        ProductResponseRecord result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when ID does not exist")
    void getProductById_NotFound_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Should successfully reduce stock when requested quantity <= available stock")
    void reduceStock_Success() {
        StockReductionRequestRecord reductionReq = new StockReductionRequestRecord(1L, 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));

        StockReductionResponseRecord response = productService.reduceStock(reductionReq);

        assertThat(response).isNotNull();
        assertThat(response.remainingStock()).isEqualTo(40);
        assertThat(sampleEntity.getStock()).isEqualTo(40);
        verify(productRepository).save(sampleEntity);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when requested quantity > available stock")
    void reduceStock_InsufficientStock_ThrowsException() {
        StockReductionRequestRecord reductionReq = new StockReductionRequestRecord(1L, 100);
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));

        assertThatThrownBy(() -> productService.reduceStock(reductionReq))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("requested 100, available 50");
    }
}
