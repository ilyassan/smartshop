package com.smartshop.service;

import com.smartshop.entity.Order;

import java.util.List;

public interface OrderService {

    Order createOrder(Long userId, List<OrderItemRequest> items, String couponCode);

    Order getOrderById(Long id);

    List<Order> getOrdersByUserId(Long userId);

    List<Order> getAllOrders();

    Order confirmOrder(Long orderId);

    Order cancelOrder(Long orderId);

    class OrderItemRequest {
        public Long productId;
        public Integer quantity;
    }
}
