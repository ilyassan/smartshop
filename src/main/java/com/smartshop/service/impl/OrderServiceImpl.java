package com.smartshop.service.impl;

import com.smartshop.entity.Order;
import com.smartshop.entity.OrderItem;
import com.smartshop.entity.Product;
import com.smartshop.enums.OrderStatus;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.OrderItemRepository;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.ProductRepository;
import com.smartshop.service.OrderService;
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

    @Override
    public Order createOrder(Long userId, List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotalHT = BigDecimal.ZERO;

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

        BigDecimal tvaRate = new BigDecimal("0.20");
        BigDecimal amountAfterDiscountHT = subtotalHT;
        BigDecimal tva = amountAfterDiscountHT.multiply(tvaRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalTTC = amountAfterDiscountHT.add(tva);

        Order order = Order.builder()
                .userId(userId)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .subtotalHT(subtotalHT)
                .loyaltyDiscount(BigDecimal.ZERO)
                .couponDiscount(BigDecimal.ZERO)
                .totalDiscount(BigDecimal.ZERO)
                .amountAfterDiscountHT(amountAfterDiscountHT)
                .tvaRate(tvaRate)
                .tva(tva)
                .totalTTC(totalTTC)
                .amountPaid(BigDecimal.ZERO)
                .remainingAmount(totalTTC)
                .stockReserved(false)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Created order with id: {} for user: {}", savedOrder.getId(), userId);

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
}
