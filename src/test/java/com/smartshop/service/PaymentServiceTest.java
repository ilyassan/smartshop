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
}
