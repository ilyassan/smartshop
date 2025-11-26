package com.smartshop.controller;

import com.smartshop.entity.Order;
import com.smartshop.enums.UserRole;
import com.smartshop.exception.UnauthorizedException;
import com.smartshop.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private static final String SESSION_USER_KEY = "LOGGED_IN_USER";
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute(SESSION_USER_KEY);
        if (loggedInUserId == null) {
            throw new UnauthorizedException("Please login first");
        }

        if (!loggedInUserId.equals(request.userId)) {
            throw new UnauthorizedException("You can only create orders for yourself");
        }

        Order order = orderService.createOrder(request.userId, request.items, request.couponCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute(SESSION_USER_KEY);
        if (loggedInUserId == null) {
            throw new UnauthorizedException("Please login first");
        }

        Order order = orderService.getOrderById(id);

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || (!userRole.equals("ADMIN") && !order.getUserId().equals(loggedInUserId))) {
            throw new UnauthorizedException("You can only view your own orders");
        }

        return ResponseEntity.ok(order);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable Long userId, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute(SESSION_USER_KEY);
        if (loggedInUserId == null) {
            throw new UnauthorizedException("Please login first");
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || (!userRole.equals("ADMIN") && !loggedInUserId.equals(userId))) {
            throw new UnauthorizedException("You can only view your own orders");
        }

        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders(HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can view all orders");
        }

        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Order> confirmOrder(@PathVariable Long id, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can confirm orders");
        }

        Order confirmedOrder = orderService.confirmOrder(id);
        return ResponseEntity.ok(confirmedOrder);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute(SESSION_USER_KEY);
        if (loggedInUserId == null) {
            throw new UnauthorizedException("Please login first");
        }

        Order order = orderService.getOrderById(id);

        if (!order.getUserId().equals(loggedInUserId)) {
            throw new UnauthorizedException("You can only cancel your own orders");
        }

        Order canceledOrder = orderService.cancelOrder(id);
        return ResponseEntity.ok(canceledOrder);
    }

    public static class CreateOrderRequest {
        public Long userId;
        public List<OrderService.OrderItemRequest> items;
        public String couponCode;
    }
}
