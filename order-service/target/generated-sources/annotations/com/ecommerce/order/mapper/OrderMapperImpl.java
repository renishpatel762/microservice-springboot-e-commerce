package com.ecommerce.order.mapper;

import com.ecommerce.order.dto.OrderItemResponseRecord;
import com.ecommerce.order.dto.OrderResponseRecord;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-18T21:57:58+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.5 (Oracle Corporation)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderResponseRecord toResponse(Order order) {
        if ( order == null ) {
            return null;
        }

        Long id = null;
        String customerName = null;
        String customerEmail = null;
        OrderStatus status = null;
        BigDecimal totalAmount = null;
        Instant createdAt = null;
        List<OrderItemResponseRecord> items = null;

        id = order.getId();
        customerName = order.getCustomerName();
        customerEmail = order.getCustomerEmail();
        status = order.getStatus();
        totalAmount = order.getTotalAmount();
        createdAt = order.getCreatedAt();
        items = toItemResponseList( order.getItems() );

        OrderResponseRecord orderResponseRecord = new OrderResponseRecord( id, customerName, customerEmail, status, totalAmount, createdAt, items );

        return orderResponseRecord;
    }

    @Override
    public OrderItemResponseRecord toItemResponse(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }

        Long id = null;
        Long productId = null;
        String productName = null;
        BigDecimal priceAtPurchase = null;
        Integer quantity = null;
        BigDecimal totalPrice = null;

        id = orderItem.getId();
        productId = orderItem.getProductId();
        productName = orderItem.getProductName();
        priceAtPurchase = orderItem.getPriceAtPurchase();
        quantity = orderItem.getQuantity();
        totalPrice = orderItem.getTotalPrice();

        OrderItemResponseRecord orderItemResponseRecord = new OrderItemResponseRecord( id, productId, productName, priceAtPurchase, quantity, totalPrice );

        return orderItemResponseRecord;
    }

    @Override
    public List<OrderItemResponseRecord> toItemResponseList(List<OrderItem> items) {
        if ( items == null ) {
            return null;
        }

        List<OrderItemResponseRecord> list = new ArrayList<OrderItemResponseRecord>( items.size() );
        for ( OrderItem orderItem : items ) {
            list.add( toItemResponse( orderItem ) );
        }

        return list;
    }
}
