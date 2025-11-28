package com.smartshop.integration;

import com.smartshop.dto.ProductDTO;
import com.smartshop.entity.Product;
import com.smartshop.entity.User;
import com.smartshop.enums.UserRole;
import com.smartshop.repository.ProductRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.util.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private Product testProduct;
    private MockHttpSession adminSession;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .build();
        adminUser = userRepository.save(adminUser);

        testProduct = Product.builder()
                .name("Test Product")
                .sku("SKU-001")
                .description("Test Description")
                .unitPrice(new BigDecimal("99.99"))
                .stock(100)
                .category("Electronics")
                .deleted(false)
                .build();
        testProduct = productRepository.save(testProduct);

        adminSession = new MockHttpSession();
        adminSession.setAttribute("LOGGED_IN_USER", adminUser.getId());
        adminSession.setAttribute("userRole", "ADMIN");
    }

    @Test
    void createProduct_Success() throws Exception {
        ProductDTO newProduct = ProductDTO.builder()
                .name("New Product")
                .sku("SKU-NEW")
                .description("New Description")
                .unitPrice(new BigDecimal("149.99"))
                .stock(50)
                .category("Gadgets")
                .build();

        mockMvc.perform(post("/products")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("New Product")))
                .andExpect(jsonPath("$.sku", is("SKU-NEW")))
                .andExpect(jsonPath("$.unitPrice", is(149.99)));
    }

    @Test
    void createProduct_DuplicateSku_BadRequest() throws Exception {
        ProductDTO newProduct = ProductDTO.builder()
                .name("Duplicate Product")
                .sku("SKU-001")
                .unitPrice(new BigDecimal("199.99"))
                .stock(20)
                .build();

        mockMvc.perform(post("/products")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getProductById_Success() throws Exception {
        mockMvc.perform(get("/products/" + testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testProduct.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Test Product")))
                .andExpect(jsonPath("$.sku", is("SKU-001")));
    }

    @Test
    void getProductById_NotFound() throws Exception {
        mockMvc.perform(get("/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllProducts_Success() throws Exception {
        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void updateProduct_Success() throws Exception {
        ProductDTO updateDTO = ProductDTO.builder()
                .name("Updated Product")
                .sku("SKU-001")
                .unitPrice(new BigDecimal("199.99"))
                .stock(150)
                .category("Updated Category")
                .build();

        mockMvc.perform(put("/products/" + testProduct.getId())
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Product")))
                .andExpect(jsonPath("$.unitPrice", is(199.99)))
                .andExpect(jsonPath("$.stock", is(150)));
    }

    @Test
    void deleteProduct_Success() throws Exception {
        mockMvc.perform(delete("/products/" + testProduct.getId())
                        .session(adminSession))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/products/" + testProduct.getId()))
                .andExpect(status().isNotFound());
    }
}
