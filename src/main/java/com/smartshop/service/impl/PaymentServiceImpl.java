package com.smartshop.service.impl;

import com.smartshop.dto.PaymentDTO;
import com.smartshop.entity.*;
import com.smartshop.enums.OrderStatus;
import com.smartshop.enums.PaymentMethod;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.mapper.PaymentMapper;
import com.smartshop.repository.*;
import com.smartshop.service.LoyaltyTierService;
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
    private final LoyaltyTierService loyaltyTierService;
    private final PaymentMapper paymentMapper;

    // Maximum payment limit for CASH payments (Article 193 CGI - Morocco)
    private static final BigDecimal CASH_PAYMENT_LIMIT = new BigDecimal("20000");

    @Override
    public PaymentDTO createPayment(PaymentDTO paymentDTO) {
        // Convert DTO to entity
        Payment payment = paymentMapper.toEntity(paymentDTO);

        // Get the order
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + payment.getOrderId()));

        // Validate cash payment limit (Art. 193 CGI)
        if (PaymentMethod.CASH.equals(payment.getPaymentMethod()) &&
                payment.getAmount().compareTo(CASH_PAYMENT_LIMIT) > 0) {
            throw new IllegalArgumentException("Cash payment cannot exceed " + CASH_PAYMENT_LIMIT + " DH (Article 193 CGI)");
        }

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

            deductStockForOrder(order);

            if (order.getCouponId() != null) {
                markCouponAsUsed(order.getCouponId());
            }

            checkAndRejectPendingOrders();
        }

        loyaltyTierService.checkAndUpgradeTier(order.getUserId());

        return paymentMapper.toDTO(savedPayment);
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
    public PaymentDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return paymentMapper.toDTO(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByOrderId(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderIdOrderByPaymentNumberAsc(orderId);
        return paymentMapper.toDTOList(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentDTO> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return paymentMapper.toDTOList(payments);
    }

    @Override
    public PaymentDTO updatePayment(Long id, PaymentDTO paymentDTO) {
        Payment existingPayment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        paymentMapper.updateEntityFromDTO(paymentDTO, existingPayment);
        Payment updatedPayment = paymentRepository.save(existingPayment);
        log.info("Updated payment with id: {}", updatedPayment.getId());

        return paymentMapper.toDTO(updatedPayment);
    }

    @Override
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        paymentRepository.delete(payment);
        log.info("Deleted payment with id: {}", id);
    }
}
