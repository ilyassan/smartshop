package com.smartshop.service;

import com.smartshop.dto.AuthResponse;
import com.smartshop.dto.LoginRequest;
import com.smartshop.dto.UserDTO;
import com.smartshop.entity.User;
import com.smartshop.enums.UserRole;
import com.smartshop.exception.UnauthorizedException;
import com.smartshop.mapper.UserMapper;
import com.smartshop.repository.UserRepository;
import com.smartshop.service.impl.AuthServiceImpl;
import com.smartshop.util.PasswordEncoder;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .role(UserRole.CLIENT)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("plainPassword");
    }

    @Test
    void login_Success() {
        UserDTO userDTO = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .role(UserRole.CLIENT)
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(true);
        when(userMapper.toDTO(user)).thenReturn(userDTO);
        doNothing().when(session).setAttribute(anyString(), any());

        AuthResponse response = authService.login(loginRequest, session);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals(UserRole.CLIENT, response.getRole());
        assertEquals("Login successful", response.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("plainPassword", "encodedPassword");
        verify(session).setAttribute("LOGGED_IN_USER", 1L);
        verify(session).setAttribute("userRole", "CLIENT");
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.login(loginRequest, session);
        });
        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(false);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.login(loginRequest, session);
        });
        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("plainPassword", "encodedPassword");
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    void getCurrentUser_Success() {
        UserDTO userDTO = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .role(UserRole.CLIENT)
                .build();

        when(session.getAttribute("LOGGED_IN_USER")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        AuthResponse response = authService.getCurrentUser(session);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals(UserRole.CLIENT, response.getRole());
        verify(session).getAttribute("LOGGED_IN_USER");
        verify(userRepository).findById(1L);
    }

    @Test
    void getCurrentUser_NotLoggedIn_ThrowsException() {
        when(session.getAttribute("LOGGED_IN_USER")).thenReturn(null);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.getCurrentUser(session);
        });
        assertEquals("Not authenticated. Please login first", exception.getMessage());
        verify(session).getAttribute("LOGGED_IN_USER");
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void getCurrentUser_UserNotFoundInDB_ThrowsException() {
        when(session.getAttribute("LOGGED_IN_USER")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            authService.getCurrentUser(session);
        });
        assertEquals("Session invalid. Please login again", exception.getMessage());
        verify(session).getAttribute("LOGGED_IN_USER");
        verify(userRepository).findById(1L);
    }

    @Test
    void logout_Success() {
        doNothing().when(session).invalidate();

        authService.logout(session);

        verify(session).invalidate();
    }
}
