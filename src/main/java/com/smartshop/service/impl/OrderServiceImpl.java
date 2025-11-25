package com.smartshop.service.impl;

import com.smartshop.entity.Coupon;
import com.smartshop.entity.Order;
import com.smartshop.entity.OrderItem;
import com.smartshop.entity.Product;
import com.smartshop.entity.User;
import com.smartshop.enums.OrderStatus;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.CouponRepository;
import com.smartshop.repository.OrderItemRepository;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.ProductRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.service.LoyaltyTierService;
import com.smartshop.service.OrderService;
import com.smartshop.util.CustomerTierDiscount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final LoyaltyTierService loyaltyTierService;

    @Override
    public Order createOrder(Long userId, List<OrderItemRequest> items, String couponCode) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // Get user to retrieve loyalty tier
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotalHT = BigDecimal.ZERO;

        // Calculate subtotal and validate stock
        for (OrderItemRequest itemRequest : items) {
            Product product = productRepository.findByIdAndDeletedFalse(itemRequest.productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemRequest.productId));

            if (product.getStock() < itemRequest.quantity) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName() +
                        ". Available: " + product.getStock() + ", Requested: " + itemRequest.quantity);
            }

            BigDecimal lineTotal = product.getUnitPrice().multiply(new BigDecimal(itemRequest.quantity));
            subtotalHT = subtotalHT.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.quantity)
                    .unitPrice(product.getUnitPrice())
                    .lineTotal(lineTotal)
                    .build();

            orderItems.add(orderItem);
        }

        // Calculate loyalty discount
        BigDecimal loyaltyDiscountAmount = CustomerTierDiscount.applyLoyaltyDiscount(user.getLoyaltyTier(), subtotalHT);

        // Calculate coupon discount and get couponId
        BigDecimal couponDiscountAmount = BigDecimal.ZERO;
        Long couponId = null;
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            Coupon coupon = couponRepository.findByCode(couponCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with code: " + couponCode));

            // Check if coupon is already used
            if (coupon.getIsUsed()) {
                throw new IllegalArgumentException("Coupon has already been used");
            }

            couponDiscountAmount = subtotalHT.multiply(coupon.getDiscountPercentage().divide(new BigDecimal("100")))
                    .setScale(2, RoundingMode.HALF_UP);

            couponId = coupon.getId();
            log.info("Applied coupon: {} (will be marked as used when payment is made)", couponCode);
        }

        // Calculate total discount and amount after discount
        BigDecimal totalDiscount = loyaltyDiscountAmount.add(couponDiscountAmount);
        BigDecimal amountAfterDiscountHT = subtotalHT.subtract(totalDiscount);

        // Calculate TVA (20%)
        BigDecimal tvaRate = new BigDecimal("0.20");
        BigDecimal tva = amountAfterDiscountHT.multiply(tvaRate).setScale(2, RoundingMode.HALF_UP);

        // Calculate final total TTC
        BigDecimal totalTTC = amountAfterDiscountHT.add(tva);

        // Create order with subtotalHT, totalTTC, remainingAmount, and couponId
        Order order = Order.builder()
                .userId(userId)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .subtotalHT(subtotalHT)
                .totalTTC(totalTTC)
                .remainingAmount(totalTTC)
                .couponId(couponId)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Created order with id: {} for user: {}. Subtotal: {}, Loyalty discount: {}, Coupon discount: {}, Total: {}",
                savedOrder.getId(), userId, subtotalHT, loyaltyDiscountAmount, couponDiscountAmount, totalTTC);

        // Save order items
        for (OrderItem item : orderItems) {
            item.setOrderId(savedOrder.getId());
            orderItemRepository.save(item);
        }

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Validate order can be confirmed
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be confirmed. Current status: " + order.getStatus());
        }

        // Check if order has at least one payment
        if (order.getRemainingAmount().compareTo(order.getTotalTTC()) == 0) {
            throw new IllegalStateException("Order must have at least one payment before confirmation");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        Order confirmedOrder = orderRepository.save(order);
        log.info("Confirmed order with id: {}", orderId);

        loyaltyTierService.checkAndUpgradeTier(order.getUserId());

        return confirmedOrder;
    }

    @Override
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Validate order can be canceled
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be canceled. Current status: " + order.getStatus());
        }

        // Check if order has any payments
        if (order.getRemainingAmount().compareTo(order.getTotalTTC()) != 0) {
            throw new IllegalStateException("Cannot cancel order that has payments. Remaining amount: " + order.getRemainingAmount() + ", Total: " + order.getTotalTTC());
        }

        order.setStatus(OrderStatus.CANCELED);
        Order canceledOrder = orderRepository.save(order);
        log.info("Canceled order with id: {}", orderId);

        return canceledOrder;
    }
}
