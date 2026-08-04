package com.ecommerce.order.service;

import com.ecommerce.order.dto.OrderCreateRequestRecord;
import com.ecommerce.order.dto.OrderResponseRecord;
import com.ecommerce.order.dto.OrderSearchCriteria;
import com.ecommerce.order.dto.PagedResponseRecord;

public interface OrderService {

    OrderResponseRecord createOrder(OrderCreateRequestRecord requestRecord);

    OrderResponseRecord getOrderById(Long id);

    OrderResponseRecord cancelOrder(Long id);

    PagedResponseRecord<OrderResponseRecord> listOrders(OrderSearchCriteria criteria);
}
