package com.smartshop.service.impl;

import com.smartshop.entity.*;
import com.smartshop.enums.OrderStatus;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.*;
import com.smartshop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    @Override
    public Payment createPayment(Payment payment) {
        // Get the order
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + payment.getOrderId()));

        // Validate payment amount doesn't exceed remaining amount
        if (payment.getAmount().compareTo(order.getRemainingAmount()) > 0) {
            throw new IllegalArgumentException("Payment amount (" + payment.getAmount() +
                    ") cannot exceed remaining amount (" + order.getRemainingAmount() + ")");
        }

        // Check if this is the first payment for the order
        List<Payment> existingPayments = paymentRepository.findByOrderId(order.getId());
        boolean isFirstPayment = existingPayments.isEmpty();

        // Auto-calculate payment number
        int paymentNumber = existingPayments.size() + 1;
        payment.setPaymentNumber(paymentNumber);

        // Save the payment
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Created payment #{} for order {} with amount: {}", paymentNumber, order.getId(), payment.getAmount());

        // Update order remaining amount
        BigDecimal newRemainingAmount = order.getRemainingAmount().subtract(payment.getAmount());
        order.setRemainingAmount(newRemainingAmount);
        orderRepository.save(order);
        log.info("Updated order {} remaining amount: {}", order.getId(), newRemainingAmount);

        // If this is the first payment, perform special actions
        if (isFirstPayment) {
            log.info("First payment for order {}, performing stock deduction and coupon marking", order.getId());

            // 1. Deduct stock for this order
            deductStockForOrder(order);

            // 2. Mark coupon as used if order has a coupon
            if (order.getCouponId() != null) {
                markCouponAsUsed(order.getCouponId());
            }

            // 3. Check other pending orders and reject if insufficient stock
            checkAndRejectPendingOrders();
        }

        return savedPayment;
    }

    private void deductStockForOrder(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());

        for (OrderItem item : orderItems) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProductId()));

            int newStock = product.getStock() - item.getQuantity();
            if (newStock < 0) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() +
                        ". Available: " + product.getStock() + ", Required: " + item.getQuantity());
            }

            product.setStock(newStock);
            productRepository.save(product);
            log.info("Deducted {} units from product {} (ID: {}). New stock: {}",
                    item.getQuantity(), product.getName(), product.getId(), newStock);
        }
    }

    private void markCouponAsUsed(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + couponId));

        coupon.setIsUsed(true);
        couponRepository.save(coupon);
        log.info("Marked coupon {} (ID: {}) as used", coupon.getCode(), coupon.getId());
    }

    private void checkAndRejectPendingOrders() {
        // Get all pending orders
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);

        for (Order pendingOrder : pendingOrders) {
            // Check if any payment has been made for this order
            List<Payment> payments = paymentRepository.findByOrderId(pendingOrder.getId());
            if (!payments.isEmpty()) {
                // Order has payments, skip it
                continue;
            }

            // Check if there's enough stock for all items in this order
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(pendingOrder.getId());
            boolean insufficientStock = false;

            for (OrderItem item : orderItems) {
                Product product = productRepository.findById(item.getProductId())
                        .orElse(null);

                if (product == null || product.getStock() < item.getQuantity()) {
                    insufficientStock = true;
                    log.info("Insufficient stock for pending order {}. Product: {}, Required: {}, Available: {}",
                            pendingOrder.getId(),
                            product != null ? product.getName() : "Unknown",
                            item.getQuantity(),
                            product != null ? product.getStock() : 0);
                    break;
                }
            }

            if (insufficientStock) {
                pendingOrder.setStatus(OrderStatus.REJECTED);
                orderRepository.save(pendingOrder);
                log.info("Rejected pending order {} due to insufficient stock", pendingOrder.getId());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderIdOrderByPaymentNumberAsc(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment updatePayment(Long id, Payment payment) {
        Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        if (payment.getAmount() != null) {
            existingPayment.setAmount(payment.getAmount());
        }
        if (payment.getPaymentMethod() != null) {
            existingPayment.setPaymentMethod(payment.getPaymentMethod());
        }
        if (payment.getPaymentDate() != null) {
            existingPayment.setPaymentDate(payment.getPaymentDate());
        }
        if (payment.getCollectionDate() != null) {
            existingPayment.setCollectionDate(payment.getCollectionDate());
        }
        if (payment.getReference() != null) {
            existingPayment.setReference(payment.getReference());
        }
        if (payment.getBankName() != null) {
            existingPayment.setBankName(payment.getBankName());
        }
        if (payment.getDueDate() != null) {
            existingPayment.setDueDate(payment.getDueDate());
        }
        if (payment.getStatus() != null) {
            existingPayment.setStatus(payment.getStatus());
        }

        Payment updatedPayment = paymentRepository.save(existingPayment);
        log.info("Updated payment with id: {}", updatedPayment.getId());

        return updatedPayment;
    }

    @Override
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        paymentRepository.delete(payment);
        log.info("Deleted payment with id: {}", id);
    }
}
