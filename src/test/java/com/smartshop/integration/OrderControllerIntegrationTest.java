package com.smartshop.integration;

import com.smartshop.dto.OrderDTO;
import com.smartshop.entity.Order;
import com.smartshop.entity.Product;
import com.smartshop.entity.User;
import com.smartshop.enums.UserRole;
import com.smartshop.enums.OrderStatus;
import com.smartshop.repository.OrderRepository;
import com.smartshop.repository.ProductRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.service.OrderService;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class OrderControllerIntegrationTest extends BaseIntegrationTest {

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
    private MockHttpSession adminSession;
    private MockHttpSession clientSession;

    @BeforeEach
    void setUp() {
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

        adminSession = new MockHttpSession();
        adminSession.setAttribute("LOGGED_IN_USER", adminUser.getId());
        adminSession.setAttribute("userRole", "ADMIN");

        clientSession = new MockHttpSession();
        clientSession.setAttribute("LOGGED_IN_USER", clientUser.getId());
        clientSession.setAttribute("userRole", "CLIENT");
    }

    @Test
    void createOrder_Success() throws Exception {
        String requestBody = "{\n" +
                "  \"userId\": " + clientUser.getId() + ",\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"productId\": " + product.getId() + ",\n" +
                "      \"quantity\": 2\n" +
                "    }\n" +
                "  ],\n" +
                "  \"couponCode\": null\n" +
                "}";

        mockMvc.perform(post("/orders")
                .session(adminSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(clientUser.getId().intValue())))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void createOrder_NotAuthenticated() throws Exception {
        String requestBody = "{\n" +
                "  \"userId\": " + clientUser.getId() + ",\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"productId\": " + product.getId() + ",\n" +
                "      \"quantity\": 1\n" +
                "    }\n" +
                "  ],\n" +
                "  \"couponCode\": null\n" +
                "}";

        mockMvc.perform(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createOrder_AsClient_Unauthorized() throws Exception {
        String requestBody = "{\n" +
                "  \"userId\": " + clientUser.getId() + ",\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"productId\": " + product.getId() + ",\n" +
                "      \"quantity\": 1\n" +
                "    }\n" +
                "  ],\n" +
                "  \"couponCode\": null\n" +
                "}";

        mockMvc.perform(post("/orders")
                .session(clientSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOrderById_AsOwner() throws Exception {
        Order order = createTestOrder(clientUser, product);

        mockMvc.perform(get("/orders/" + order.getId())
                .session(clientSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(order.getId().intValue())))
                .andExpect(jsonPath("$.userId", is(clientUser.getId().intValue())));
    }

    @Test
    void getOrderById_AsAdmin() throws Exception {
        Order order = createTestOrder(clientUser, product);

        mockMvc.perform(get("/orders/" + order.getId())
                .session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(order.getId().intValue())));
    }

    @Test
    void getOrderById_AsOtherUser_Unauthorized() throws Exception {
        Order order = createTestOrder(clientUser, product);

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

        mockMvc.perform(get("/orders/" + order.getId())
                .session(anotherClientSession))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOrderById_NotAuthenticated() throws Exception {
        Order order = createTestOrder(clientUser, product);

        mockMvc.perform(get("/orders/" + order.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOrdersByUserId_AsOwner() throws Exception {
        Order order1 = createTestOrder(clientUser, product);
        Order order2 = createTestOrder(clientUser, product);

        mockMvc.perform(get("/orders/user/" + clientUser.getId())
                .session(clientSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getOrdersByUserId_AsAdmin() throws Exception {
        Order order = createTestOrder(clientUser, product);

        mockMvc.perform(get("/orders/user/" + clientUser.getId())
                .session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getOrdersByUserId_AsOtherUser_Unauthorized() throws Exception {
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

        Order order = createTestOrder(clientUser, product);

        mockMvc.perform(get("/orders/user/" + clientUser.getId())
                .session(anotherClientSession))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOrdersByUserId_NotAuthenticated() throws Exception {
        mockMvc.perform(get("/orders/user/" + clientUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllOrders_AsAdmin() throws Exception {
        createTestOrder(clientUser, product);
        createTestOrder(clientUser, product);

        mockMvc.perform(get("/orders")
                .session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllOrders_AsClient_Unauthorized() throws Exception {
        mockMvc.perform(get("/orders")
                .session(clientSession))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllOrders_NotAuthenticated() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void cancelOrder_AsOwner() throws Exception {
        Order order = createTestOrder(clientUser, product);

        mockMvc.perform(put("/orders/" + order.getId() + "/cancel")
                .session(clientSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELED")));
    }

    @Test
    void cancelOrder_AsOtherUser_Unauthorized() throws Exception {
        Order order = createTestOrder(clientUser, product);

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

        mockMvc.perform(put("/orders/" + order.getId() + "/cancel")
                .session(anotherClientSession))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cancelOrder_NotAuthenticated() throws Exception {
        Order order = createTestOrder(clientUser, product);

        mockMvc.perform(put("/orders/" + order.getId() + "/cancel"))
                .andExpect(status().isUnauthorized());
    }

    private Order createTestOrder(User user, Product product) {
        Order order = Order.builder()
                .userId(user.getId())
                .orderDate(java.time.LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .subtotalHT(new BigDecimal("199.98"))
                .totalTTC(new BigDecimal("239.98"))
                .remainingAmount(new BigDecimal("239.98"))
                .build();
        return orderRepository.save(order);
    }
}
