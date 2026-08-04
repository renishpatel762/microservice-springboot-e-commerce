package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.dto.OrderCreateRequestRecord;
import com.ecommerce.order.dto.OrderItemRequestRecord;
import com.ecommerce.order.dto.OrderResponseRecord;
import com.ecommerce.order.dto.ProductDto;
import com.ecommerce.order.dto.StockReductionDto;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.exception.InsufficientStockException;
import com.ecommerce.order.exception.OrderCancellationException;
import com.ecommerce.order.exception.ProductNotFoundException;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private ProductDto sampleProduct;
    private OrderCreateRequestRecord sampleOrderRequest;
    private Order sampleOrderEntity;
    private OrderResponseRecord sampleOrderResponse;

    @BeforeEach
    void setUp() {
        sampleProduct = new ProductDto(
                1L, "Mechanical Keyboard", "RGB Gaming", new BigDecimal("99.99"), 50, "Electronics"
        );

        sampleOrderRequest = new OrderCreateRequestRecord(
                "Jane Doe",
                "jane.doe@example.com",
                List.of(new OrderItemRequestRecord(1L, 2))
        );

        sampleOrderEntity = Order.builder()
                .id(100L)
                .customerName("Jane Doe")
                .customerEmail("jane.doe@example.com")
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("199.98"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        sampleOrderResponse = new OrderResponseRecord(
                100L,
                "Jane Doe",
                "jane.doe@example.com",
                OrderStatus.CONFIRMED,
                new BigDecimal("199.98"),
                Instant.now(),
                List.of()
        );
    }

    @Test
    @DisplayName("Should successfully create order when products exist and have sufficient stock")
    void createOrder_Success() {
        StockReductionDto.Response reductionResponse = new StockReductionDto.Response(
                1L, "Mechanical Keyboard", new BigDecimal("99.99"), 48
        );

        when(productServiceClient.getProductById(1L)).thenReturn(sampleProduct);
        when(productServiceClient.reduceStock(eq(1L), eq(2))).thenReturn(reductionResponse);
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrderEntity);
        when(orderMapper.toResponse(sampleOrderEntity)).thenReturn(sampleOrderResponse);

        OrderResponseRecord result = orderService.createOrder(sampleOrderRequest);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.status()).isEqualTo(OrderStatus.CONFIRMED);
        verify(productServiceClient).reduceStock(1L, 2);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product does not exist in Product Service")
    void createOrder_ProductNotFound_ThrowsException() {
        when(productServiceClient.getProductById(1L)).thenThrow(new ProductNotFoundException(1L));

        assertThatThrownBy(() -> orderService.createOrder(sampleOrderRequest))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when requested quantity exceeds available stock")
    void createOrder_InsufficientStock_ThrowsException() {
        ProductDto lowStockProduct = new ProductDto(
                1L, "Mechanical Keyboard", "RGB Gaming", new BigDecimal("99.99"), 1, "Electronics"
        );
        when(productServiceClient.getProductById(1L)).thenReturn(lowStockProduct);

        assertThatThrownBy(() -> orderService.createOrder(sampleOrderRequest))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("requested 2, available 1");
    }

    @Test
    @DisplayName("Should successfully cancel an active order")
    void cancelOrder_Success() {
        Order activeOrder = Order.builder()
                .id(100L)
                .status(OrderStatus.CONFIRMED)
                .build();

        Order cancelledOrder = Order.builder()
                .id(100L)
                .status(OrderStatus.CANCELLED)
                .build();

        OrderResponseRecord cancelledResponse = new OrderResponseRecord(
                100L, "Jane Doe", "jane.doe@example.com", OrderStatus.CANCELLED, new BigDecimal("199.98"), Instant.now(), List.of()
        );

        when(orderRepository.findById(100L)).thenReturn(Optional.of(activeOrder));
        when(orderRepository.save(activeOrder)).thenReturn(cancelledOrder);
        when(orderMapper.toResponse(cancelledOrder)).thenReturn(cancelledResponse);

        OrderResponseRecord response = orderService.cancelOrder(100L);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Should throw OrderCancellationException when order is already cancelled")
    void cancelOrder_AlreadyCancelled_ThrowsException() {
        Order cancelledOrder = Order.builder()
                .id(100L)
                .status(OrderStatus.CANCELLED)
                .build();

        when(orderRepository.findById(100L)).thenReturn(Optional.of(cancelledOrder));

        assertThatThrownBy(() -> orderService.cancelOrder(100L))
                .isInstanceOf(OrderCancellationException.class)
                .hasMessageContaining("already cancelled");
    }
}
