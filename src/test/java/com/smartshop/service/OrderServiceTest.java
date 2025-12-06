package com.smartshop.service;

import com.smartshop.dto.OrderDTO;
import com.smartshop.entity.*;
import com.smartshop.enums.OrderStatus;
import com.smartshop.enums.UserRole;
import com.smartshop.enums.CustomerTier;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.mapper.OrderMapper;
import com.smartshop.repository.*;
import com.smartshop.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private LoyaltyTierService loyaltyTierService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Product product;
    private Order order;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(UserRole.CLIENT)
                .loyaltyTier(CustomerTier.BASIC)
                .build();

        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TEST-001")
                .unitPrice(new BigDecimal("100.00"))
                .stock(10)
                .deleted(false)
                .build();

        order = Order.builder()
                .id(1L)
                .userId(1L)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .subtotalHT(new BigDecimal("100.00"))
                .totalTTC(new BigDecimal("120.00"))
                .remainingAmount(new BigDecimal("120.00"))
                .build();

        orderDTO = OrderDTO.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .subtotalHT(new BigDecimal("100.00"))
                .totalTTC(new BigDecimal("120.00"))
                .build();
    }

    // Test: Create order successfully
    @Test
    void testCreateOrder_Success() {
        OrderService.OrderItemRequest item = new OrderService.OrderItemRequest();
        item.productId = 1L;
        item.quantity = 2;
        List<OrderService.OrderItemRequest> items = List.of(item);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(OrderItem.builder().build());
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        OrderDTO result = orderService.createOrder(1L, items, null);

        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
    }

    // Test: Create order with empty items
    @Test
    void testCreateOrder_EmptyItems() {
        List<OrderService.OrderItemRequest> items = new ArrayList<>();

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(1L, items, null));
    }

    // Test: Create order with null items
    @Test
    void testCreateOrder_NullItems() {
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(1L, null, null));
    }

    // Test: User not found
    @Test
    void testCreateOrder_UserNotFound() {
        OrderService.OrderItemRequest item = new OrderService.OrderItemRequest();
        item.productId = 1L;
        item.quantity = 2;
        List<OrderService.OrderItemRequest> items = List.of(item);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(1L, items, null));
    }

    // Test: Product not found
    @Test
    void testCreateOrder_ProductNotFound() {
        OrderService.OrderItemRequest item = new OrderService.OrderItemRequest();
        item.productId = 1L;
        item.quantity = 2;
        List<OrderService.OrderItemRequest> items = List.of(item);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(1L, items, null));
    }

    // Test: Insufficient stock
    @Test
    void testCreateOrder_InsufficientStock() {
        Product lowStockProduct = Product.builder()
                .id(1L)
                .stock(1)
                .name("Low Stock Product")
                .deleted(false)
                .build();

        OrderService.OrderItemRequest item = new OrderService.OrderItemRequest();
        item.productId = 1L;
        item.quantity = 5;
        List<OrderService.OrderItemRequest> items = List.of(item);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(lowStockProduct));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(1L, items, null));
    }

    // Test: Create order with valid coupon
    @Test
    void testCreateOrder_WithValidCoupon() {
        Coupon coupon = Coupon.builder()
                .id(1L)
                .code("PROMO-TEST")
                .discountPercentage(new BigDecimal("10"))
                .isUsed(false)
                .build();

        OrderService.OrderItemRequest item = new OrderService.OrderItemRequest();
        item.productId = 1L;
        item.quantity = 2;
        List<OrderService.OrderItemRequest> items = List.of(item);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(couponRepository.findByCode("PROMO-TEST")).thenReturn(Optional.of(coupon));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(OrderItem.builder().build());
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        OrderDTO result = orderService.createOrder(1L, items, "PROMO-TEST");

        assertNotNull(result);
        verify(couponRepository).findByCode("PROMO-TEST");
    }

    // Test: Coupon not found
    @Test
    void testCreateOrder_CouponNotFound() {
        OrderService.OrderItemRequest item = new OrderService.OrderItemRequest();
        item.productId = 1L;
        item.quantity = 2;
        List<OrderService.OrderItemRequest> items = List.of(item);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(1L, items, "INVALID"));
    }

    // Test: Coupon already used
    @Test
    void testCreateOrder_CouponAlreadyUsed() {
        Coupon usedCoupon = Coupon.builder()
                .id(1L)
                .code("PROMO-TEST")
                .isUsed(true)
                .build();

        OrderService.OrderItemRequest item = new OrderService.OrderItemRequest();
        item.productId = 1L;
        item.quantity = 2;
        List<OrderService.OrderItemRequest> items = List.of(item);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(couponRepository.findByCode("PROMO-TEST")).thenReturn(Optional.of(usedCoupon));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(1L, items, "PROMO-TEST"));
    }

    // Test: Get order by ID
    @Test
    void testGetOrderById_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        OrderDTO result = orderService.getOrderById(1L);

        assertNotNull(result);
        verify(orderRepository).findById(1L);
    }

    // Test: Get order by ID not found
    @Test
    void testGetOrderById_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById(1L));
    }

    // Test: Get orders by user ID
    @Test
    void testGetOrdersByUserId_Success() {
        List<Order> orders = List.of(order);

        when(orderRepository.findByUserId(1L)).thenReturn(orders);
        when(orderMapper.toDTOList(orders)).thenReturn(List.of(orderDTO));

        List<OrderDTO> result = orderService.getOrdersByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // Test: Get all orders
    @Test
    void testGetAllOrders_Success() {
        List<Order> orders = List.of(order);

        when(orderRepository.findAll()).thenReturn(orders);
        when(orderMapper.toDTOList(orders)).thenReturn(List.of(orderDTO));

        List<OrderDTO> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // Test: Confirm order successfully
    @Test
    void testConfirmOrder_Success() {
        order.setRemainingAmount(new BigDecimal("50.00"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        OrderDTO result = orderService.confirmOrder(1L);

        assertNotNull(result);
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CONFIRMED));
    }

    // Test: Confirm order not found
    @Test
    void testConfirmOrder_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.confirmOrder(1L));
    }

    // Test: Confirm order not in pending status
    @Test
    void testConfirmOrder_NotPending() {
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.confirmOrder(1L));
    }

    // Test: Confirm order without any payment
    @Test
    void testConfirmOrder_NoPaymentMade() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.confirmOrder(1L));
    }

    // Test: Cancel order successfully
    @Test
    void testCancelOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        OrderDTO result = orderService.cancelOrder(1L);

        assertNotNull(result);
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CANCELED));
    }

    // Test: Cancel order not found
    @Test
    void testCancelOrder_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.cancelOrder(1L));
    }

    // Test: Cancel order not in pending status
    @Test
    void testCancelOrder_NotPending() {
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(1L));
    }

    // Test: Cancel order with payments
    @Test
    void testCancelOrder_HasPayments() {
        order.setRemainingAmount(new BigDecimal("50.00"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(1L));
    }

    // Test: Create order with multiple items
    @Test
    void testCreateOrder_MultipleItems() {
        Product product2 = Product.builder()
                .id(2L)
                .name("Product 2")
                .unitPrice(new BigDecimal("50.00"))
                .stock(20)
                .deleted(false)
                .build();

        OrderService.OrderItemRequest item1 = new OrderService.OrderItemRequest();
        item1.productId = 1L;
        item1.quantity = 2;
        OrderService.OrderItemRequest item2 = new OrderService.OrderItemRequest();
        item2.productId = 2L;
        item2.quantity = 3;
        List<OrderService.OrderItemRequest> items = List.of(item1, item2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(product));
        when(productRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.of(product2));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(OrderItem.builder().build());
        when(orderMapper.toDTO(order)).thenReturn(orderDTO);

        OrderDTO result = orderService.createOrder(1L, items, null);

        assertNotNull(result);
        verify(orderItemRepository, times(2)).save(any(OrderItem.class));
    }
}
