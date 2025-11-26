package com.smartshop.service;

import com.smartshop.dto.OrderDTO;

import java.util.List;

public interface OrderService {

    OrderDTO createOrder(Long userId, List<OrderItemRequest> items, String couponCode);

    OrderDTO getOrderById(Long id);

    List<OrderDTO> getOrdersByUserId(Long userId);

    List<OrderDTO> getAllOrders();

    OrderDTO confirmOrder(Long orderId);

    OrderDTO cancelOrder(Long orderId);

    class OrderItemRequest {
        public Long productId;
        public Integer quantity;
    }
}
