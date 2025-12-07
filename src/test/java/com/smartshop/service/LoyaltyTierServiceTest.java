package com.smartshop.service;

import com.smartshop.entity.Order;
import com.smartshop.entity.Payment;
import com.smartshop.entity.User;
import com.smartshop.enums.CustomerTier;
import com.smartshop.enums.OrderStatus;
import com.smartshop.enums.PaymentMethod;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.PaymentRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.service.impl.LoyaltyTierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoyaltyTierServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private LoyaltyTierServiceImpl loyaltyTierService;

    private User user;
    private Order order1;
    private Order order2;
    private Order order3;
    private Payment payment1;
    private Payment payment2;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .loyaltyTier(CustomerTier.BASIC)
                .build();

        order1 = Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.CONFIRMED)
                .build();

        order2 = Order.builder()
                .id(2L)
                .userId(1L)
                .status(OrderStatus.CONFIRMED)
                .build();

        order3 = Order.builder()
                .id(3L)
                .userId(1L)
                .status(OrderStatus.CONFIRMED)
                .build();

        payment1 = Payment.builder()
                .id(1L)
                .orderId(1L)
                .amount(new BigDecimal("500.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .paymentDate(LocalDate.now())
                .build();

        payment2 = Payment.builder()
                .id(2L)
                .orderId(1L)
                .amount(new BigDecimal("600.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .paymentDate(LocalDate.now())
                .build();
    }

    // Test: upgradeTierIfEligible - User not found
    @Test
    void testUpgradeTierIfEligible_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loyaltyTierService.upgradeTierIfEligible(1L));
    }

    // Test: upgradeTierIfEligible - No upgrade needed (below all thresholds)
    @Test
    void testUpgradeTierIfEligible_NoUpgradeNeeded() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdAndStatus(1L, OrderStatus.CONFIRMED)).thenReturn(List.of()); // 0 confirmed orders
        when(orderRepository.findByUserId(1L)).thenReturn(new ArrayList<>()); // 0 spending

        User result = loyaltyTierService.upgradeTierIfEligible(1L);

        assertEquals(CustomerTier.BASIC, result.getLoyaltyTier());
        verify(userRepository, never()).save(any(User.class));
    }

    // Test: upgradeTierIfEligible - Upgrade to SILVER via orders (3+ confirmed orders)
    @Test
    void testUpgradeTierIfEligible_UpgradeToSilverViaOrders() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdAndStatus(1L, OrderStatus.CONFIRMED)).thenReturn(List.of(order1, order2, order3)); // 3 orders
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order1, order2, order3));
        when(paymentRepository.findByOrderId(anyLong())).thenReturn(new ArrayList<>());

        User upgradedUser = User.builder()
                .id(1L)
                .username("testuser")
                .loyaltyTier(CustomerTier.SILVER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(upgradedUser);

        User result = loyaltyTierService.upgradeTierIfEligible(1L);

        assertEquals(CustomerTier.SILVER, result.getLoyaltyTier());
        verify(userRepository).save(argThat(u -> u.getLoyaltyTier() == CustomerTier.SILVER));
    }

    // Test: upgradeTierIfEligible - Upgrade to SILVER via spending (1000+ total)
    @Test
    void testUpgradeTierIfEligible_UpgradeToSilverViaSpending() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdAndStatus(1L, OrderStatus.CONFIRMED)).thenReturn(List.of(order1)); // 1 order
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order1));
        when(paymentRepository.findByOrderId(1L)).thenReturn(List.of(payment1, payment2)); // 500 + 600 = 1100

        User upgradedUser = User.builder()
                .id(1L)
                .username("testuser")
                .loyaltyTier(CustomerTier.SILVER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(upgradedUser);

        User result = loyaltyTierService.upgradeTierIfEligible(1L);

        assertEquals(CustomerTier.SILVER, result.getLoyaltyTier());
        verify(userRepository).save(argThat(u -> u.getLoyaltyTier() == CustomerTier.SILVER));
    }

    // Test: upgradeTierIfEligible - Upgrade to GOLD via orders (10+ confirmed orders)
    @Test
    void testUpgradeTierIfEligible_UpgradeToGoldViaOrders() {
        user.setLoyaltyTier(CustomerTier.SILVER);

        List<Order> tenOrders = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            tenOrders.add(Order.builder()
                    .id((long) i)
                    .userId(1L)
                    .status(OrderStatus.CONFIRMED)
                    .build());
        }

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdAndStatus(1L, OrderStatus.CONFIRMED)).thenReturn(tenOrders); // 10 orders
        when(orderRepository.findByUserId(1L)).thenReturn(tenOrders);
        when(paymentRepository.findByOrderId(anyLong())).thenReturn(new ArrayList<>());

        User upgradedUser = User.builder()
                .id(1L)
                .username("testuser")
                .loyaltyTier(CustomerTier.GOLD)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(upgradedUser);

        User result = loyaltyTierService.upgradeTierIfEligible(1L);

        assertEquals(CustomerTier.GOLD, result.getLoyaltyTier());
        verify(userRepository).save(argThat(u -> u.getLoyaltyTier() == CustomerTier.GOLD));
    }

    // Test: upgradeTierIfEligible - Upgrade to GOLD via spending (5000+ total)
    @Test
    void testUpgradeTierIfEligible_UpgradeToGoldViaSpending() {
        user.setLoyaltyTier(CustomerTier.SILVER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdAndStatus(1L, OrderStatus.CONFIRMED)).thenReturn(List.of(order1)); // 1 order
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order1));

        Payment highPayment = Payment.builder()
                .id(3L)
                .orderId(1L)
                .amount(new BigDecimal("5100.00"))
                .build();

        when(paymentRepository.findByOrderId(1L)).thenReturn(List.of(highPayment)); // 5100

        User upgradedUser = User.builder()
                .id(1L)
                .username("testuser")
                .loyaltyTier(CustomerTier.GOLD)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(upgradedUser);

        User result = loyaltyTierService.upgradeTierIfEligible(1L);

        assertEquals(CustomerTier.GOLD, result.getLoyaltyTier());
        verify(userRepository).save(argThat(u -> u.getLoyaltyTier() == CustomerTier.GOLD));
    }

    // Test: upgradeTierIfEligible - Upgrade to PLATINUM via orders (20+ confirmed orders)
    @Test
    void testUpgradeTierIfEligible_UpgradeToPlatinumViaOrders() {
        user.setLoyaltyTier(CustomerTier.GOLD);

        List<Order> twentyOrders = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            twentyOrders.add(Order.builder()
                    .id((long) i)
                    .userId(1L)
                    .status(OrderStatus.CONFIRMED)
                    .build());
        }

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdAndStatus(1L, OrderStatus.CONFIRMED)).thenReturn(twentyOrders); // 20 orders
        when(orderRepository.findByUserId(1L)).thenReturn(twentyOrders);
        when(paymentRepository.findByOrderId(anyLong())).thenReturn(new ArrayList<>());

        User upgradedUser = User.builder()
                .id(1L)
                .username("testuser")
                .loyaltyTier(CustomerTier.PLATINUM)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(upgradedUser);

        User result = loyaltyTierService.upgradeTierIfEligible(1L);

        assertEquals(CustomerTier.PLATINUM, result.getLoyaltyTier());
        verify(userRepository).save(argThat(u -> u.getLoyaltyTier() == CustomerTier.PLATINUM));
    }

    // Test: upgradeTierIfEligible - Upgrade to PLATINUM via spending (15000+ total)
    @Test
    void testUpgradeTierIfEligible_UpgradeToPlatinumViaSpending() {
        user.setLoyaltyTier(CustomerTier.GOLD);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdAndStatus(1L, OrderStatus.CONFIRMED)).thenReturn(List.of(order1)); // 1 order
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order1));

        Payment platinumPayment = Payment.builder()
                .id(4L)
                .orderId(1L)
                .amount(new BigDecimal("15100.00"))
                .build();

        when(paymentRepository.findByOrderId(1L)).thenReturn(List.of(platinumPayment)); // 15100

        User upgradedUser = User.builder()
                .id(1L)
                .username("testuser")
                .loyaltyTier(CustomerTier.PLATINUM)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(upgradedUser);

        User result = loyaltyTierService.upgradeTierIfEligible(1L);

        assertEquals(CustomerTier.PLATINUM, result.getLoyaltyTier());
        verify(userRepository).save(argThat(u -> u.getLoyaltyTier() == CustomerTier.PLATINUM));
    }

    // Test: upgradeTierIfEligible - User with null loyalty tier (should default to BASIC)
    @Test
    void testUpgradeTierIfEligible_NullLoyaltyTierDefaultsToBasic() {
        user.setLoyaltyTier(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdAndStatus(1L, OrderStatus.CONFIRMED)).thenReturn(List.of(order1, order2, order3)); // 3 orders
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order1, order2, order3));
        when(paymentRepository.findByOrderId(anyLong())).thenReturn(new ArrayList<>());

        User upgradedUser = User.builder()
                .id(1L)
                .username("testuser")
                .loyaltyTier(CustomerTier.SILVER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(upgradedUser);

        User result = loyaltyTierService.upgradeTierIfEligible(1L);

        assertEquals(CustomerTier.SILVER, result.getLoyaltyTier());
        verify(userRepository).save(any(User.class));
    }

    // Test: checkAndUpgradeTier - Calls upgradeTierIfEligible
    @Test
    void testCheckAndUpgradeTier_CallsUpgradeTierIfEligible() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdAndStatus(1L, OrderStatus.CONFIRMED)).thenReturn(new ArrayList<>());
        when(orderRepository.findByUserId(1L)).thenReturn(new ArrayList<>());

        loyaltyTierService.checkAndUpgradeTier(1L);

        verify(userRepository).findById(1L);
    }

    // Test: upgradeTierIfEligible - No downgrade (only upgrades allowed)
    @Test
    void testUpgradeTierIfEligible_NoDowngrade() {
        user.setLoyaltyTier(CustomerTier.PLATINUM);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdAndStatus(1L, OrderStatus.CONFIRMED)).thenReturn(List.of()); // 0 orders
        when(orderRepository.findByUserId(1L)).thenReturn(new ArrayList<>()); // 0 spending

        User result = loyaltyTierService.upgradeTierIfEligible(1L);

        assertEquals(CustomerTier.PLATINUM, result.getLoyaltyTier());
        verify(userRepository, never()).save(any(User.class)); // Should NOT be saved
    }

    // Test: upgradeTierIfEligible - Multiple payments for single order
    @Test
    void testUpgradeTierIfEligible_MultiplePaymentsCalculatedCorrectly() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findByUserIdAndStatus(1L, OrderStatus.CONFIRMED)).thenReturn(List.of(order1));
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order1));

        Payment p1 = Payment.builder().id(1L).orderId(1L).amount(new BigDecimal("300.00")).build();
        Payment p2 = Payment.builder().id(2L).orderId(1L).amount(new BigDecimal("400.00")).build();
        Payment p3 = Payment.builder().id(3L).orderId(1L).amount(new BigDecimal("350.00")).build();

        when(paymentRepository.findByOrderId(1L)).thenReturn(List.of(p1, p2, p3)); // 300 + 400 + 350 = 1050

        User upgradedUser = User.builder()
                .id(1L)
                .username("testuser")
                .loyaltyTier(CustomerTier.SILVER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(upgradedUser);

        User result = loyaltyTierService.upgradeTierIfEligible(1L);

        assertEquals(CustomerTier.SILVER, result.getLoyaltyTier());
        verify(userRepository).save(any(User.class));
    }
}
