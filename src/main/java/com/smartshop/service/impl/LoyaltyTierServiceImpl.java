package com.smartshop.service.impl;

import com.smartshop.entity.Order;
import com.smartshop.entity.Payment;
import com.smartshop.entity.User;
import com.smartshop.enums.CustomerTier;
import com.smartshop.enums.OrderStatus;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.PaymentRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.service.LoyaltyTierService;
import com.smartshop.util.LoyaltyTierRules;
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
public class LoyaltyTierServiceImpl implements LoyaltyTierService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public void checkAndUpgradeTier(Long userId) {
        upgradeTierIfEligible(userId);
    }

    @Override
    public User upgradeTierIfEligible(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        int confirmedOrdersCount = countConfirmedOrders(userId);

        BigDecimal totalSpending = calculateTotalSpending(userId);

        CustomerTier currentTier = user.getLoyaltyTier();
        if (currentTier == null) {
            currentTier = CustomerTier.BASIC;
        }

        if (LoyaltyTierRules.needsUpgrade(currentTier, totalSpending, confirmedOrdersCount)) {
            CustomerTier newTier = LoyaltyTierRules.getUpgradedTier(totalSpending, confirmedOrdersCount);
            user.setLoyaltyTier(newTier);
            User savedUser = userRepository.save(user);
            log.info("Upgraded user {} from {} to {}. Total spending: {}, Confirmed orders: {}",
                    userId, currentTier, newTier, totalSpending, confirmedOrdersCount);
            return savedUser;
        }

        return user;
    }

    private int countConfirmedOrders(Long userId) {
        List<Order> confirmedOrders = orderRepository.findByUserIdAndStatus(userId, OrderStatus.CONFIRMED);
        return confirmedOrders.size();
    }

    private BigDecimal calculateTotalSpending(Long userId) {
        List<Order> allOrders = orderRepository.findByUserId(userId);

        BigDecimal totalSpending = BigDecimal.ZERO;

        for (Order order : allOrders) {
            List<Payment> payments = paymentRepository.findByOrderId(order.getId());
            for (Payment payment : payments) {
                totalSpending = totalSpending.add(payment.getAmount());
            }
        }

        return totalSpending;
    }
}
