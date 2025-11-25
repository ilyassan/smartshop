package com.smartshop.controller;

import com.smartshop.entity.Order;
import com.smartshop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request.userId, request.items, request.couponCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Order> confirmOrder(@PathVariable Long id) {
        Order confirmedOrder = orderService.confirmOrder(id);
        return ResponseEntity.ok(confirmedOrder);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        Order canceledOrder = orderService.cancelOrder(id);
        return ResponseEntity.ok(canceledOrder);
    }

    public static class CreateOrderRequest {
        public Long userId;
        public List<OrderService.OrderItemRequest> items;
        public String couponCode;
    }
}
