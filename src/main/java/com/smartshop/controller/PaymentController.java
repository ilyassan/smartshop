package com.smartshop.controller;

import com.smartshop.annotation.RequireAuth;
import com.smartshop.annotation.RequireRole;
import com.smartshop.dto.OrderDTO;
import com.smartshop.dto.PaymentDTO;
import com.smartshop.exception.UnauthorizedException;
import com.smartshop.service.OrderService;
import com.smartshop.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private static final String SESSION_USER_KEY = "LOGGED_IN_USER";
    private final PaymentService paymentService;
    private final OrderService orderService;

    @PostMapping
    @RequireRole("ADMIN")
    public ResponseEntity<PaymentDTO> createPayment(@Valid @RequestBody PaymentDTO payment) {
        OrderDTO orderDTO = orderService.getOrderById(payment.getOrderId());

        log.info("Creating payment for order: {}", payment.getOrderId());
        PaymentDTO createdPayment = paymentService.createPayment(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPayment);
    }

    @GetMapping("/{id}")
    @RequireAuth
    public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable Long id, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute(SESSION_USER_KEY);

        PaymentDTO payment = paymentService.getPaymentById(id);
        OrderDTO order = orderService.getOrderById(payment.getOrderId());

        String userRole = (String) session.getAttribute("userRole");
        if (!userRole.equals("ADMIN") && !order.getUserId().equals(loggedInUserId)) {
            throw new UnauthorizedException("You can only view payments for your own orders");
        }

        log.info("Fetching payment with id: {}", id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/order/{orderId}")
    @RequireAuth
    public ResponseEntity<List<PaymentDTO>> getPaymentsByOrderId(@PathVariable Long orderId, HttpSession session) {
        Long loggedInUserId = (Long) session.getAttribute(SESSION_USER_KEY);

        OrderDTO order = orderService.getOrderById(orderId);

        String userRole = (String) session.getAttribute("userRole");
        if (!userRole.equals("ADMIN") && !order.getUserId().equals(loggedInUserId)) {
            throw new UnauthorizedException("You can only view payments for your own orders");
        }

        log.info("Fetching payments for order: {}", orderId);
        List<PaymentDTO> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping
    @RequireRole("ADMIN")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        log.info("Fetching all payments");
        List<PaymentDTO> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<PaymentDTO> updatePayment(@PathVariable Long id, @Valid @RequestBody PaymentDTO payment) {
        log.info("Updating payment with id: {}", id);
        PaymentDTO updatedPayment = paymentService.updatePayment(id, payment);
        return ResponseEntity.ok(updatedPayment);
    }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        log.info("Deleting payment with id: {}", id);
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}
