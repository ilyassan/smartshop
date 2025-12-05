package com.smartshop.integration;

import com.smartshop.dto.PaymentDTO;
import com.smartshop.entity.Order;
import com.smartshop.entity.Payment;
import com.smartshop.entity.Product;
import com.smartshop.entity.User;
import com.smartshop.enums.UserRole;
import com.smartshop.enums.OrderStatus;
import com.smartshop.enums.PaymentMethod;
import com.smartshop.enums.PaymentStatus;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.PaymentRepository;
import com.smartshop.repository.ProductRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.util.PasswordEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PaymentControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User clientUser;
    private Product product;
    private Order order;
    private MockHttpSession adminSession;
    private MockHttpSession clientSession;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
        productRepository.deleteAll();

        adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .name("Admin User")
                .email("admin@example.com")
                .build();
        adminUser = userRepository.save(adminUser);

        clientUser = User.builder()
                .username("client1")
                .password(passwordEncoder.encode("client123"))
                .role(UserRole.CLIENT)
                .name("Client User")
                .email("client@example.com")
                .build();
        clientUser = userRepository.save(clientUser);

        product = Product.builder()
                .name("Test Product")
                .sku("TEST-001")
                .unitPrice(new BigDecimal("99.99"))
                .stock(100)
                .deleted(false)
                .build();
        product = productRepository.save(product);

        order = Order.builder()
                .userId(clientUser.getId())
                .orderDate(java.time.LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .subtotalHT(new BigDecimal("199.98"))
                .totalTTC(new BigDecimal("239.98"))
                .remainingAmount(new BigDecimal("239.98"))
                .build();
        order = orderRepository.save(order);

        adminSession = new MockHttpSession();
        adminSession.setAttribute("LOGGED_IN_USER", adminUser.getId());
        adminSession.setAttribute("userRole", "ADMIN");

        clientSession = new MockHttpSession();
        clientSession.setAttribute("LOGGED_IN_USER", clientUser.getId());
        clientSession.setAttribute("userRole", "CLIENT");
    }

    @Test
    void createPayment_Success() throws Exception {
        PaymentDTO paymentDTO = PaymentDTO.builder()
                .orderId(order.getId())
                .paymentNumber(1)
                .amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .paymentDate(LocalDate.now())
                .reference("REF123456")
                .status(PaymentStatus.PENDING)
                .build();

        mockMvc.perform(post("/payments")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId", is(order.getId().intValue())))
                .andExpect(jsonPath("$.amount", is(100.0)));
    }

    @Test
    void createPayment_NotAuthenticated() throws Exception {
        PaymentDTO paymentDTO = PaymentDTO.builder()
                .orderId(order.getId())
                .paymentNumber(1)
                .amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .paymentDate(LocalDate.now())
                .reference("REF123456")
                .status(PaymentStatus.PENDING)
                .build();

        mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPayment_AsClient_Unauthorized() throws Exception {
        PaymentDTO paymentDTO = PaymentDTO.builder()
                .orderId(order.getId())
                .paymentNumber(1)
                .amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .paymentDate(LocalDate.now())
                .reference("REF123456")
                .status(PaymentStatus.PENDING)
                .build();

        mockMvc.perform(post("/payments")
                .session(clientSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPaymentById_AsOwner() throws Exception {
        Payment payment = createTestPayment(order);

        mockMvc.perform(get("/payments/" + payment.getId())
                .session(clientSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(payment.getId().intValue())))
                .andExpect(jsonPath("$.orderId", is(order.getId().intValue())));
    }

    @Test
    void getPaymentById_AsAdmin() throws Exception {
        Payment payment = createTestPayment(order);

        mockMvc.perform(get("/payments/" + payment.getId())
                .session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(payment.getId().intValue())));
    }

    @Test
    void getPaymentById_AsOtherUser_Unauthorized() throws Exception {
        Payment payment = createTestPayment(order);

        User anotherClient = User.builder()
                .username("client2")
                .password(passwordEncoder.encode("client123"))
                .role(UserRole.CLIENT)
                .name("Another Client")
                .email("client2@example.com")
                .build();
        anotherClient = userRepository.save(anotherClient);

        MockHttpSession anotherClientSession = new MockHttpSession();
        anotherClientSession.setAttribute("LOGGED_IN_USER", anotherClient.getId());
        anotherClientSession.setAttribute("userRole", "CLIENT");

        mockMvc.perform(get("/payments/" + payment.getId())
                .session(anotherClientSession))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPaymentById_NotAuthenticated() throws Exception {
        Payment payment = createTestPayment(order);

        mockMvc.perform(get("/payments/" + payment.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPaymentsByOrderId_AsOwner() throws Exception {
        Payment payment1 = createTestPayment(order);
        Payment payment2 = createTestPayment(order);

        mockMvc.perform(get("/payments/order/" + order.getId())
                .session(clientSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getPaymentsByOrderId_AsAdmin() throws Exception {
        Payment payment = createTestPayment(order);

        mockMvc.perform(get("/payments/order/" + order.getId())
                .session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getPaymentsByOrderId_AsOtherUser_Unauthorized() throws Exception {
        User anotherClient = User.builder()
                .username("client2")
                .password(passwordEncoder.encode("client123"))
                .role(UserRole.CLIENT)
                .name("Another Client")
                .email("client2@example.com")
                .build();
        anotherClient = userRepository.save(anotherClient);

        MockHttpSession anotherClientSession = new MockHttpSession();
        anotherClientSession.setAttribute("LOGGED_IN_USER", anotherClient.getId());
        anotherClientSession.setAttribute("userRole", "CLIENT");

        Payment payment = createTestPayment(order);

        mockMvc.perform(get("/payments/order/" + order.getId())
                .session(anotherClientSession))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPaymentsByOrderId_NotAuthenticated() throws Exception {
        mockMvc.perform(get("/payments/order/" + order.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllPayments_AsAdmin() throws Exception {
        createTestPayment(order);
        createTestPayment(order);

        mockMvc.perform(get("/payments")
                .session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllPayments_AsClient_Unauthorized() throws Exception {
        mockMvc.perform(get("/payments")
                .session(clientSession))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllPayments_NotAuthenticated() throws Exception {
        mockMvc.perform(get("/payments"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void deletePayment_AsAdmin() throws Exception {
        Payment payment = createTestPayment(order);

        mockMvc.perform(delete("/payments/" + payment.getId())
                .session(adminSession))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePayment_AsClient_Unauthorized() throws Exception {
        Payment payment = createTestPayment(order);

        mockMvc.perform(delete("/payments/" + payment.getId())
                .session(clientSession))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deletePayment_NotAuthenticated() throws Exception {
        Payment payment = createTestPayment(order);

        mockMvc.perform(delete("/payments/" + payment.getId()))
                .andExpect(status().isUnauthorized());
    }

    private Payment createTestPayment(Order order) {
        Payment payment = Payment.builder()
                .orderId(order.getId())
                .paymentNumber(1)
                .amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.TRANSFER)
                .paymentDate(LocalDate.now())
                .reference("REF123456")
                .status(PaymentStatus.PENDING)
                .build();
        return paymentRepository.save(payment);
    }
}
