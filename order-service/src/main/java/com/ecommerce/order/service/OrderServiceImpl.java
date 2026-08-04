package com.ecommerce.order.service;

import com.ecommerce.order.client.ProductServiceClient;
import com.ecommerce.order.dto.OrderCreateRequestRecord;
import com.ecommerce.order.dto.OrderItemRequestRecord;
import com.ecommerce.order.dto.OrderResponseRecord;
import com.ecommerce.order.dto.OrderSearchCriteria;
import com.ecommerce.order.dto.PagedResponseRecord;
import com.ecommerce.order.dto.ProductDto;
import com.ecommerce.order.dto.StockReductionDto;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.enums.OrderStatus;
import com.ecommerce.order.exception.InsufficientStockException;
import com.ecommerce.order.exception.OrderCancellationException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.mapper.OrderMapper;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.OrderSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ===================================================================================
 * ORDER SERVICE IMPLEMENTATION & ENTERPRISE ARCHITECTURE PATTERNS:
 * ===================================================================================
 * 1. PRODUCT SNAPSHOTTING PATTERN:
 *    When an order is created, product details (productName, priceAtPurchase) must be snapshotted
 *    and stored inside the OrderItem table. If the seller updates the product name or price in the
 *    Product Service next month, historical customer receipts/invoices must NOT change!
 *
 * 2. DISTRIBUTED INVENTORY VALIDATION & REDUCTION:
 *    - Validate product availability with Product Service.
 *    - Atomically reduce inventory stock before creating the persistent Order entity.
 *    - If stock reduction fails (due to insufficient stock), abort transaction and fail fast.
 * ===================================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponseRecord createOrder(final OrderCreateRequestRecord requestRecord) {
        log.info("Processing order creation for customer: '{}' ({}) with {} items",
                requestRecord.customerName(), requestRecord.customerEmail(), requestRecord.items().size());

        Order order = Order.builder()
                .customerName(requestRecord.customerName())
                .customerEmail(requestRecord.customerEmail())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal cumulativeTotal = BigDecimal.ZERO;

        // Process and validate each item against Product Service REST client
        for (OrderItemRequestRecord itemReq : requestRecord.items()) {
            // Step 1: Pre-validate product exists and inspect current stock
            ProductDto productDto = productServiceClient.getProductById(itemReq.productId());

            if (productDto.stock() < itemReq.quantity()) {
                throw new InsufficientStockException(String.format(
                        "Insufficient stock for product '%s' (ID %d): requested %d, available %d",
                        productDto.name(), productDto.id(), itemReq.quantity(), productDto.stock()));
            }

            // Step 2: Perform stock deduction in Product Service
            StockReductionDto.Response stockReduction = productServiceClient.reduceStock(
                    itemReq.productId(), itemReq.quantity());

            // Step 3: Calculate line total and create snapshotted OrderItem
            BigDecimal priceAtPurchase = stockReduction.price();
            BigDecimal itemTotalPrice = priceAtPurchase.multiply(BigDecimal.valueOf(itemReq.quantity()));
            cumulativeTotal = cumulativeTotal.add(itemTotalPrice);

            OrderItem orderItem = OrderItem.builder()
                    .productId(stockReduction.productId())
                    .productName(stockReduction.name())
                    .priceAtPurchase(priceAtPurchase)
                    .quantity(itemReq.quantity())
                    .totalPrice(itemTotalPrice)
                    .build();

            order.addOrderItem(orderItem);
        }

        order.setTotalAmount(cumulativeTotal);
        order.setStatus(OrderStatus.CONFIRMED);

        Order savedOrder = orderRepository.save(order);
        log.info("Successfully created and confirmed Order ID: {} for total amount: ${}", savedOrder.getId(), savedOrder.getTotalAmount());

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseRecord getOrderById(final Long id) {
        log.debug("Fetching order by ID: {}", id);

        return orderRepository.findById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    @Transactional
    public OrderResponseRecord cancelOrder(final Long id) {
        log.info("Processing order cancellation for Order ID: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderCancellationException("Order ID " + id + " is already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updatedOrder = orderRepository.save(order);

        log.info("Successfully cancelled Order ID: {}", updatedOrder.getId());
        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseRecord<OrderResponseRecord> listOrders(final OrderSearchCriteria criteria) {
        log.debug("Listing orders with criteria: {}", criteria);

        Sort.Direction direction = Sort.Direction.fromString(criteria.sortDir());
        Sort sort = Sort.by(direction, criteria.sortBy());
        Pageable pageable = PageRequest.of(criteria.page(), criteria.size(), sort);

        Specification<Order> spec = OrderSpecification.buildSpecification(criteria);
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderResponseRecord> content = orderPage.getContent()
                .stream()
                .map(orderMapper::toResponse)
                .toList();

        return new PagedResponseRecord<>(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast()
        );
    }
}
