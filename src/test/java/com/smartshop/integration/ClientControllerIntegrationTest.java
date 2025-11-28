package com.smartshop.integration;

import com.smartshop.dto.UserDTO;
import com.smartshop.entity.User;
import com.smartshop.enums.CustomerTier;
import com.smartshop.enums.UserRole;
import com.smartshop.repository.UserRepository;
import com.smartshop.util.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClientControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User clientUser;
    private MockHttpSession adminSession;
    private MockHttpSession clientSession;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

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
                .loyaltyTier(CustomerTier.BASIC)
                .build();
        clientUser = userRepository.save(clientUser);

        adminSession = new MockHttpSession();
        adminSession.setAttribute("LOGGED_IN_USER", adminUser.getId());
        adminSession.setAttribute("userRole", "ADMIN");

        clientSession = new MockHttpSession();
        clientSession.setAttribute("LOGGED_IN_USER", clientUser.getId());
        clientSession.setAttribute("userRole", "CLIENT");
    }

    @Test
    void createClient_Success() throws Exception {
        UserDTO newClient = UserDTO.builder()
                .username("newclient")
                .password("password123")
                .role(UserRole.CLIENT)
                .name("New Client")
                .email("newclient@example.com")
                .phone("1234567890")
                .build();

        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newClient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("newclient")))
                .andExpect(jsonPath("$.name", is("New Client")))
                .andExpect(jsonPath("$.email", is("newclient@example.com")))
                .andExpect(jsonPath("$.role", is("CLIENT")))
                .andExpect(jsonPath("$.password", is(nullValue())));
    }

    @Test
    void getClientById_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/clients/" + clientUser.getId())
                        .session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(clientUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is("client1")))
                .andExpect(jsonPath("$.password", is(nullValue())));
    }

    @Test
    void getClientById_AsOwnProfile_Success() throws Exception {
        mockMvc.perform(get("/clients/" + clientUser.getId())
                        .session(clientSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(clientUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is("client1")));
    }

    @Test
    void getClientById_AsOtherClient_Unauthorized() throws Exception {
        mockMvc.perform(get("/clients/" + adminUser.getId())
                        .session(clientSession))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getClientById_NotLoggedIn_Unauthorized() throws Exception {
        mockMvc.perform(get("/clients/" + clientUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllClients_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/clients")
                        .session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void getAllClients_AsClient_Unauthorized() throws Exception {
        mockMvc.perform(get("/clients")
                        .session(clientSession))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateClient_Success() throws Exception {
        UserDTO updateDTO = UserDTO.builder()
                .username(clientUser.getUsername())
                .role(UserRole.CLIENT)
                .name("Updated Name")
                .email("updated@example.com")
                .phone("9876543210")
                .build();

        mockMvc.perform(put("/clients/" + clientUser.getId())
                        .session(clientSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.email", is("updated@example.com")));
    }

    @Test
    void updateClient_AsOtherUser_Unauthorized() throws Exception {
        UserDTO updateDTO = UserDTO.builder()
                .username(adminUser.getUsername())
                .role(UserRole.ADMIN)
                .name("Updated Name")
                .build();

        mockMvc.perform(put("/clients/" + adminUser.getId())
                        .session(clientSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteClient_AsAdmin_Success() throws Exception {
        mockMvc.perform(delete("/clients/" + clientUser.getId())
                        .session(adminSession))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteClient_AsClient_Unauthorized() throws Exception {
        mockMvc.perform(delete("/clients/" + clientUser.getId())
                        .session(clientSession))
                .andExpect(status().isUnauthorized());
    }
}
