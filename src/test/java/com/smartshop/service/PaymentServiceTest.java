package com.smartshop.service;

import com.smartshop.dto.PaymentDTO;
import com.smartshop.entity.*;
import com.smartshop.enums.OrderStatus;
import com.smartshop.enums.PaymentMethod;
import com.smartshop.enums.PaymentStatus;
import com.smartshop.enums.UserRole;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.mapper.PaymentMapper;
import com.smartshop.repository.*;
import com.smartshop.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private LoyaltyTierService loyaltyTierService;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Order order;
    private Payment payment;
    private PaymentDTO paymentDTO;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id(1L)
                .userId(1L)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .subtotalHT(new BigDecimal("100.00"))
                .totalTTC(new BigDecimal("120.00"))
                .remainingAmount(new BigDecimal("120.00"))
                .build();

        payment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .paymentNumber(1)
                .amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .paymentDate(LocalDate.now())
                .reference("REF123456")
                .status(PaymentStatus.PENDING)
                .build();

        paymentDTO = PaymentDTO.builder()
                .id(1L)
                .orderId(1L)
                .paymentNumber(1)
                .amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .paymentDate(LocalDate.now())
                .reference("REF123456")
                .status(PaymentStatus.PENDING)
                .build();
    }

    // Test: Create payment successfully
    @Test
    void testCreatePayment_Success() {
        when(paymentMapper.toEntity(paymentDTO)).thenReturn(payment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(new ArrayList<>());
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(new ArrayList<>());
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(new ArrayList<>());
        when(paymentMapper.toDTO(payment)).thenReturn(paymentDTO);

        PaymentDTO result = paymentService.createPayment(paymentDTO);

        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
        verify(paymentRepository).save(any(Payment.class));
    }

    // Test: Order not found
    @Test
    void testCreatePayment_OrderNotFound() {
        when(paymentMapper.toEntity(paymentDTO)).thenReturn(payment);
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.createPayment(paymentDTO));
    }

    // Test: Cash payment exceeds limit (20000 DH)
    @Test
    void testCreatePayment_CashExceedsLimit() {
        Payment cashPayment = Payment.builder()
                .orderId(1L)
                .amount(new BigDecimal("25000.00"))
                .paymentMethod(PaymentMethod.CASH)
                .build();

        when(paymentMapper.toEntity(paymentDTO)).thenReturn(cashPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(paymentDTO));
    }

    // Test: Payment amount exceeds remaining amount
    @Test
    void testCreatePayment_AmountExceedsRemaining() {
        Payment excessPayment = Payment.builder()
                .orderId(1L)
                .amount(new BigDecimal("150.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .build();

        when(paymentMapper.toEntity(paymentDTO)).thenReturn(excessPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(paymentDTO));
    }

    // Test: Get payment by ID
    @Test
    void testGetPaymentById_Success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(paymentDTO);

        PaymentDTO result = paymentService.getPaymentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    // Test: Get payment by ID not found
    @Test
    void testGetPaymentById_NotFound() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.getPaymentById(1L));
    }

    // Test: Get payments by order ID
    @Test
    void testGetPaymentsByOrderId_Success() {
        List<Payment> payments = List.of(payment);

        when(paymentRepository.findByOrderIdOrderByPaymentNumberAsc(1L)).thenReturn(payments);
        when(paymentMapper.toDTOList(payments)).thenReturn(List.of(paymentDTO));

        List<PaymentDTO> result = paymentService.getPaymentsByOrderId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // Test: Get all payments
    @Test
    void testGetAllPayments_Success() {
        List<Payment> payments = List.of(payment);

        when(paymentRepository.findAll()).thenReturn(payments);
        when(paymentMapper.toDTOList(payments)).thenReturn(List.of(paymentDTO));

        List<PaymentDTO> result = paymentService.getAllPayments();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // Test: Update payment
    @Test
    void testUpdatePayment_Success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.toDTO(payment)).thenReturn(paymentDTO);

        PaymentDTO result = paymentService.updatePayment(1L, paymentDTO);

        assertNotNull(result);
        verify(paymentRepository).save(any(Payment.class));
    }

    // Test: Update payment not found
    @Test
    void testUpdatePayment_NotFound() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.updatePayment(1L, paymentDTO));
    }

    // Test: Delete payment
    @Test
    void testDeletePayment_Success() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        paymentService.deletePayment(1L);

        verify(paymentRepository).delete(payment);
    }

    // Test: Delete payment not found
    @Test
    void testDeletePayment_NotFound() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.deletePayment(1L));
    }

    // Test: Cash payment within limit
    @Test
    void testCreatePayment_CashWithinLimit() {
        Payment cashPayment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .paymentNumber(1)
                .amount(new BigDecimal("15000.00"))
                .paymentMethod(PaymentMethod.CASH)
                .paymentDate(LocalDate.now())
                .reference("CASH001")
                .status(PaymentStatus.PENDING)
                .build();

        order.setRemainingAmount(new BigDecimal("15000.00"));

        when(paymentMapper.toEntity(any())).thenReturn(cashPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(new ArrayList<>());
        when(paymentRepository.save(any(Payment.class))).thenReturn(cashPayment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(new ArrayList<>());
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(new ArrayList<>());
        when(paymentMapper.toDTO(cashPayment)).thenReturn(PaymentDTO.builder()
                .id(1L)
                .orderId(1L)
                .amount(new BigDecimal("15000.00"))
                .paymentMethod(PaymentMethod.CASH)
                .build());

        PaymentDTO result = paymentService.createPayment(new PaymentDTO());

        assertNotNull(result);
    }

    // Test: deductStockForOrder() - Stock deduction on first payment
    @Test
    void testCreatePayment_FirstPayment_DeductsStock() {
        Product product1 = Product.builder()
                .id(1L)
                .name("Product 1")
                .stock(10)
                .deleted(false)
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("Product 2")
                .stock(20)
                .deleted(false)
                .build();

        OrderItem item1 = OrderItem.builder()
                .orderId(1L)
                .productId(1L)
                .quantity(3)
                .build();

        OrderItem item2 = OrderItem.builder()
                .orderId(1L)
                .productId(2L)
                .quantity(5)
                .build();

        Payment firstPayment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .status(PaymentStatus.PENDING)
                .build();

        PaymentDTO firstPaymentDTO = PaymentDTO.builder()
                .id(1L)
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .build();

        when(paymentMapper.toEntity(firstPaymentDTO)).thenReturn(firstPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(new ArrayList<>()); // First payment
        when(paymentRepository.save(any(Payment.class))).thenReturn(firstPayment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item1, item2));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(new ArrayList<>());
        when(paymentMapper.toDTO(firstPayment)).thenReturn(firstPaymentDTO);

        paymentService.createPayment(firstPaymentDTO);

        // Verify stock was deducted
        verify(productRepository, times(2)).save(any(Product.class));
    }

    // Test: deductStockForOrder() - Insufficient stock throws exception
    @Test
    void testCreatePayment_FirstPayment_InsufficientStockThrows() {
        Product lowStockProduct = Product.builder()
                .id(1L)
                .name("Low Stock Product")
                .stock(2)
                .deleted(false)
                .build();

        OrderItem item = OrderItem.builder()
                .orderId(1L)
                .productId(1L)
                .quantity(5) // More than available stock
                .build();

        Payment firstPayment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .status(PaymentStatus.PENDING)
                .build();

        PaymentDTO firstPaymentDTO = PaymentDTO.builder()
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .build();

        when(paymentMapper.toEntity(firstPaymentDTO)).thenReturn(firstPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(new ArrayList<>()); // First payment
        when(paymentRepository.save(any(Payment.class))).thenReturn(firstPayment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item));
        when(productRepository.findById(1L)).thenReturn(Optional.of(lowStockProduct));

        assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(firstPaymentDTO));
    }

    // Test: markCouponAsUsed() - Coupon marked on first payment
    @Test
    void testCreatePayment_FirstPayment_MarksCouponAsUsed() {
        Coupon coupon = Coupon.builder()
                .id(1L)
                .code("PROMO-FIRST")
                .isUsed(false)
                .build();

        order.setCouponId(1L);

        Payment firstPayment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .status(PaymentStatus.PENDING)
                .build();

        PaymentDTO firstPaymentDTO = PaymentDTO.builder()
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .build();

        when(paymentMapper.toEntity(firstPaymentDTO)).thenReturn(firstPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(new ArrayList<>()); // First payment
        when(paymentRepository.save(any(Payment.class))).thenReturn(firstPayment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(new ArrayList<>());
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(new ArrayList<>());
        when(paymentMapper.toDTO(firstPayment)).thenReturn(firstPaymentDTO);

        paymentService.createPayment(firstPaymentDTO);

        // Verify coupon was marked as used
        verify(couponRepository).save(argThat(c -> c.getIsUsed() == true));
    }

    // Test: markCouponAsUsed() - Coupon not found throws exception
    @Test
    void testCreatePayment_FirstPayment_CouponNotFoundThrows() {
        order.setCouponId(999L); // Non-existent coupon

        Payment firstPayment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .status(PaymentStatus.PENDING)
                .build();

        PaymentDTO firstPaymentDTO = PaymentDTO.builder()
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .build();

        when(paymentMapper.toEntity(firstPaymentDTO)).thenReturn(firstPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(new ArrayList<>()); // First payment
        when(paymentRepository.save(any(Payment.class))).thenReturn(firstPayment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(new ArrayList<>());
        when(couponRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.createPayment(firstPaymentDTO));
    }

    // Test: checkAndRejectPendingOrders() - Rejects orders with insufficient stock
    @Test
    void testCreatePayment_FirstPayment_RejectsPendingOrdersWithInsufficientStock() {
        Product product = Product.builder()
                .id(1L)
                .name("Limited Stock Product")
                .stock(5)
                .deleted(false)
                .build();

        // Current order items (will reduce product stock from 5 to 2)
        OrderItem item1 = OrderItem.builder()
                .orderId(1L)
                .productId(1L)
                .quantity(3)
                .build();

        // Pending order that will need 4 units (insufficient after current order pays)
        Order pendingOrder = Order.builder()
                .id(2L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .build();

        OrderItem pendingItem = OrderItem.builder()
                .orderId(2L)
                .productId(1L)
                .quantity(4) // More than remaining stock (2)
                .build();

        Payment firstPayment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .status(PaymentStatus.PENDING)
                .build();

        PaymentDTO firstPaymentDTO = PaymentDTO.builder()
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .build();

        when(paymentMapper.toEntity(firstPaymentDTO)).thenReturn(firstPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(new ArrayList<>()); // First payment
        when(paymentRepository.save(any(Payment.class))).thenReturn(firstPayment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item1));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(pendingOrder));
        when(paymentRepository.findByOrderId(2L)).thenReturn(new ArrayList<>()); // No payment for pending order
        when(orderItemRepository.findByOrderId(2L)).thenReturn(List.of(pendingItem));
        when(paymentMapper.toDTO(firstPayment)).thenReturn(firstPaymentDTO);

        paymentService.createPayment(firstPaymentDTO);

        // Verify pending order was rejected
        verify(orderRepository).save(argThat(o -> o.getId().equals(2L) && o.getStatus() == OrderStatus.REJECTED));
    }

    // Test: checkAndRejectPendingOrders() - Does not reject orders with sufficient stock
    @Test
    void testCreatePayment_FirstPayment_DoesNotRejectOrdersWithSufficientStock() {
        Product product = Product.builder()
                .id(1L)
                .name("Adequate Stock Product")
                .stock(20)
                .deleted(false)
                .build();

        OrderItem item1 = OrderItem.builder()
                .orderId(1L)
                .productId(1L)
                .quantity(3)
                .build();

        Order pendingOrder = Order.builder()
                .id(2L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .build();

        OrderItem pendingItem = OrderItem.builder()
                .orderId(2L)
                .productId(1L)
                .quantity(5) // Sufficient stock remains (20 - 3 = 17)
                .build();

        Payment firstPayment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .status(PaymentStatus.PENDING)
                .build();

        PaymentDTO firstPaymentDTO = PaymentDTO.builder()
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .build();

        when(paymentMapper.toEntity(firstPaymentDTO)).thenReturn(firstPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(new ArrayList<>()); // First payment
        when(paymentRepository.save(any(Payment.class))).thenReturn(firstPayment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item1));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(pendingOrder));
        when(paymentRepository.findByOrderId(2L)).thenReturn(new ArrayList<>()); // No payment for pending order
        when(orderItemRepository.findByOrderId(2L)).thenReturn(List.of(pendingItem));
        when(paymentMapper.toDTO(firstPayment)).thenReturn(firstPaymentDTO);

        paymentService.createPayment(firstPaymentDTO);

        // Verify pending order was NOT rejected (only the current order is saved)
        verify(orderRepository, times(1)).save(any(Order.class)); // Only 1 save: current order remaining amount
    }

    // Test: checkAndRejectPendingOrders() - Skips orders with existing payments
    @Test
    void testCreatePayment_FirstPayment_SkipsPendingOrdersWithPayments() {
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .stock(5)
                .deleted(false)
                .build();

        OrderItem item1 = OrderItem.builder()
                .orderId(1L)
                .productId(1L)
                .quantity(3)
                .build();

        Order pendingOrder = Order.builder()
                .id(2L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .build();

        // Payment already exists for pending order (so it should be skipped)
        Payment existingPayment = Payment.builder()
                .id(2L)
                .orderId(2L)
                .amount(new BigDecimal("30.00"))
                .build();

        Payment firstPayment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .status(PaymentStatus.PENDING)
                .build();

        PaymentDTO firstPaymentDTO = PaymentDTO.builder()
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .build();

        when(paymentMapper.toEntity(firstPaymentDTO)).thenReturn(firstPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(new ArrayList<>()); // First payment for order 1
        when(paymentRepository.save(any(Payment.class))).thenReturn(firstPayment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item1));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(pendingOrder));
        when(paymentRepository.findByOrderId(2L)).thenReturn(List.of(existingPayment)); // Has payment, should skip
        when(paymentMapper.toDTO(firstPayment)).thenReturn(firstPaymentDTO);

        paymentService.createPayment(firstPaymentDTO);

        // Verify pending order was NOT rejected (has existing payment, so skipped)
        verify(orderRepository, times(1)).save(any(Order.class)); // Only current order, no rejection save
    }

    // Test: Second payment does not trigger helper methods
    @Test
    void testCreatePayment_SecondPayment_DoesNotTriggerHelperMethods() {
        Payment existingPayment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .amount(new BigDecimal("60.00"))
                .build();

        Payment secondPayment = Payment.builder()
                .id(2L)
                .orderId(1L)
                .paymentNumber(2)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .status(PaymentStatus.PENDING)
                .build();

        PaymentDTO secondPaymentDTO = PaymentDTO.builder()
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .build();

        when(paymentMapper.toEntity(secondPaymentDTO)).thenReturn(secondPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(List.of(existingPayment)); // Not first payment
        when(paymentRepository.save(any(Payment.class))).thenReturn(secondPayment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentMapper.toDTO(secondPayment)).thenReturn(secondPaymentDTO);

        paymentService.createPayment(secondPaymentDTO);

        // Verify helper methods were not called (only 1 orderRepository save for remaining amount)
        verify(orderItemRepository, never()).findByOrderId(1L);
        verify(couponRepository, never()).findById(any());
    }

    // Test: deductStockForOrder - Product not found (lambda exception coverage)
    @Test
    void testCreatePayment_FirstPayment_ProductNotFoundDuringDeduction() {
        OrderItem item = OrderItem.builder()
                .orderId(1L)
                .productId(999L) // Non-existent product
                .quantity(1)
                .build();

        Payment firstPayment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .status(PaymentStatus.PENDING)
                .build();

        PaymentDTO firstPaymentDTO = PaymentDTO.builder()
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .build();

        when(paymentMapper.toEntity(firstPaymentDTO)).thenReturn(firstPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(new ArrayList<>()); // First payment
        when(paymentRepository.save(any(Payment.class))).thenReturn(firstPayment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item));
        when(productRepository.findById(999L)).thenReturn(Optional.empty()); // Product not found

        assertThrows(ResourceNotFoundException.class, () -> paymentService.createPayment(firstPaymentDTO));
    }

    // Test: markCouponAsUsed - Handle order without coupon
    @Test
    void testCreatePayment_FirstPayment_NoCouponAttached() {
        order.setCouponId(null);

        OrderItem item = OrderItem.builder()
                .orderId(1L)
                .productId(1L)
                .quantity(1)
                .build();

        Product product = Product.builder()
                .id(1L)
                .name("Test")
                .stock(5)
                .deleted(false)
                .build();

        Payment firstPayment = Payment.builder()
                .id(1L)
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .status(PaymentStatus.PENDING)
                .build();

        PaymentDTO firstPaymentDTO = PaymentDTO.builder()
                .orderId(1L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .build();

        when(paymentMapper.toEntity(firstPaymentDTO)).thenReturn(firstPayment);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(new ArrayList<>()); // First payment
        when(paymentRepository.save(any(Payment.class))).thenReturn(firstPayment);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findByOrderId(1L)).thenReturn(List.of(item));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(new ArrayList<>());
        when(paymentMapper.toDTO(firstPayment)).thenReturn(firstPaymentDTO);

        PaymentDTO result = paymentService.createPayment(firstPaymentDTO);

        assertNotNull(result);
        // Verify couponRepository was never called since order has no coupon
        verify(couponRepository, never()).findById(any());
    }
}
