package com.smartshop.controller;

import com.smartshop.annotation.RequireAuth;
import com.smartshop.annotation.RequireRole;
import com.smartshop.dto.OrderDTO;
import com.smartshop.enums.UserRole;
import com.smartshop.exception.UnauthorizedException;
import com.smartshop.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
    @RequireRole("ADMIN")
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderDTO order = orderService.createOrder(request.userId, request.items, request.couponCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    @RequireAuth
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute(SESSION_USER_KEY);

        OrderDTO order = orderService.getOrderById(id);

        String userRole = (String) session.getAttribute("userRole");
        if (!userRole.equals("ADMIN") && !order.getUserId().equals(loggedInUserId)) {
            throw new UnauthorizedException("You can only view your own orders");
        }

        return ResponseEntity.ok(order);
    }

    @GetMapping("/user/{userId}")
    @RequireAuth
    public ResponseEntity<List<OrderDTO>> getOrdersByUserId(@PathVariable Long userId, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute(SESSION_USER_KEY);

        String userRole = (String) session.getAttribute("userRole");
        if (!userRole.equals("ADMIN") && !loggedInUserId.equals(userId)) {
            throw new UnauthorizedException("You can only view your own orders");
        }

        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping
    @RequireRole("ADMIN")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/confirm")
    @RequireRole("ADMIN")
    public ResponseEntity<OrderDTO> confirmOrder(@PathVariable Long id) {
        OrderDTO confirmedOrder = orderService.confirmOrder(id);
        return ResponseEntity.ok(confirmedOrder);
    }

    @PutMapping("/{id}/cancel")
    @RequireAuth
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long id, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute(SESSION_USER_KEY);

        OrderDTO order = orderService.getOrderById(id);

        if (!order.getUserId().equals(loggedInUserId)) {
            throw new UnauthorizedException("You can only cancel your own orders");
        }

        OrderDTO canceledOrder = orderService.cancelOrder(id);
        return ResponseEntity.ok(canceledOrder);
    }

    public static class CreateOrderRequest {
        public Long userId;
        public List<OrderService.OrderItemRequest> items;
        public String couponCode;
    }
}
