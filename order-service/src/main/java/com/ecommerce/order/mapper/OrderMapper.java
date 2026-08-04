package com.ecommerce.order.mapper;

import com.ecommerce.order.dto.OrderItemResponseRecord;
import com.ecommerce.order.dto.OrderResponseRecord;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct interface for converting Order entities and items to immutable response DTO records.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderMapper {

    OrderResponseRecord toResponse(Order order);

    OrderItemResponseRecord toItemResponse(OrderItem orderItem);

    List<OrderItemResponseRecord> toItemResponseList(List<OrderItem> items);
}
