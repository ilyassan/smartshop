package com.smartshop.controller;

import com.smartshop.entity.Order;
import com.smartshop.entity.Payment;
import com.smartshop.exception.UnauthorizedException;
import com.smartshop.service.OrderService;
import com.smartshop.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private static final String SESSION_USER_KEY = "LOGGED_IN_USER";
    private final PaymentService paymentService;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute(SESSION_USER_KEY);
        if (loggedInUserId == null) {
            throw new UnauthorizedException("Please login first");
        }

        Order order = orderService.getOrderById(payment.getOrderId());

        if (!order.getUserId().equals(loggedInUserId)) {
            throw new UnauthorizedException("You can only create payments for your own orders");
        }

        log.info("Creating payment for order: {}", payment.getOrderId());
        Payment createdPayment = paymentService.createPayment(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute(SESSION_USER_KEY);
        if (loggedInUserId == null) {
            throw new UnauthorizedException("Please login first");
        }

        Payment payment = paymentService.getPaymentById(id);
        Order order = orderService.getOrderById(payment.getOrderId());

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || (!userRole.equals("ADMIN") && !order.getUserId().equals(loggedInUserId))) {
            throw new UnauthorizedException("You can only view payments for your own orders");
        }

        log.info("Fetching payment with id: {}", id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrderId(@PathVariable Long orderId, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute(SESSION_USER_KEY);
        if (loggedInUserId == null) {
            throw new UnauthorizedException("Please login first");
        }

        Order order = orderService.getOrderById(orderId);

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || (!userRole.equals("ADMIN") && !order.getUserId().equals(loggedInUserId))) {
            throw new UnauthorizedException("You can only view payments for your own orders");
        }

        log.info("Fetching payments for order: {}", orderId);
        List<Payment> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments(HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can view all payments");
        }

        log.info("Fetching all payments");
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> updatePayment(@PathVariable Long id, @RequestBody Payment payment, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can update payments");
        }

        log.info("Updating payment with id: {}", id);
        Payment updatedPayment = paymentService.updatePayment(id, payment);
        return ResponseEntity.ok(updatedPayment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can delete payments");
        }

        log.info("Deleting payment with id: {}", id);
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}
