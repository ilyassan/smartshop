package com.smartshop.integration;

import com.smartshop.dto.LoginRequest;
import com.smartshop.entity.User;
import com.smartshop.enums.UserRole;
import com.smartshop.repository.UserRepository;
import com.smartshop.util.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("testpass123"))
                .role(UserRole.CLIENT)
                .name("Test User")
                .email("test@example.com")
                .build();
        userRepository.save(testUser);

        session = new MockHttpSession();
    }

    @Test
    void login_Success() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("testpass123");

        mockMvc.perform(post("/auth/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.role", is("CLIENT")))
                .andExpect(jsonPath("$.message", is("Login successful")));
    }

    @Test
    void login_InvalidUsername() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wronguser");
        loginRequest.setPassword("testpass123");

        mockMvc.perform(post("/auth/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_InvalidPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCurrentUser_Success() throws Exception {
        session.setAttribute("LOGGED_IN_USER", testUser.getId());

        mockMvc.perform(get("/auth/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.role", is("CLIENT")));
    }

    @Test
    void getCurrentUser_NotAuthenticated() throws Exception {
        mockMvc.perform(get("/auth/me")
                        .session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_Success() throws Exception {
        session.setAttribute("LOGGED_IN_USER", testUser.getId());

        mockMvc.perform(post("/auth/logout")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Logout successful")));
    }
}
