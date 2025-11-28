package com.smartshop.service;

import com.smartshop.dto.UserDTO;
import com.smartshop.entity.User;
import com.smartshop.enums.CustomerTier;
import com.smartshop.enums.UserRole;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.mapper.UserMapper;
import com.smartshop.repository.UserRepository;
import com.smartshop.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ClientServiceImpl clientService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .role(UserRole.CLIENT)
                .name("Test User")
                .email("test@example.com")
                .phone("1234567890")
                .address("123 Test St")
                .loyaltyTier(CustomerTier.BASIC)
                .build();

        userDTO = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .password("plainPassword")
                .role(UserRole.CLIENT)
                .name("Test User")
                .email("test@example.com")
                .phone("1234567890")
                .address("123 Test St")
                .loyaltyTier(CustomerTier.BASIC)
                .build();
    }

    @Test
    void createClient_Success() {
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(false);
        when(userMapper.toEntity(userDTO)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = clientService.createClient(userDTO);

        assertNotNull(result);
        assertNull(result.getPassword());
        assertEquals(UserRole.CLIENT, result.getRole());
        verify(userRepository).existsByUsername(userDTO.getUsername());
        verify(passwordEncoder).encode(anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createClient_UsernameAlreadyExists_ThrowsException() {
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            clientService.createClient(userDTO);
        });
        verify(userRepository).existsByUsername(userDTO.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getClientById_Success() {
        when(userRepository.findByIdAndRole(1L, UserRole.CLIENT)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        UserDTO result = clientService.getClientById(1L);

        assertNotNull(result);
        assertNull(result.getPassword());
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByIdAndRole(1L, UserRole.CLIENT);
    }

    @Test
    void getClientById_NotFound_ThrowsException() {
        when(userRepository.findByIdAndRole(anyLong(), eq(UserRole.CLIENT))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.getClientById(999L);
        });
        verify(userRepository).findByIdAndRole(999L, UserRole.CLIENT);
    }

    @Test
    void getAllClients_Success() {
        UserDTO dtoWithPassword = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .password("hashedPassword")
                .role(UserRole.CLIENT)
                .name("Test User")
                .email("test@example.com")
                .build();

        List<User> users = Arrays.asList(user);
        when(userRepository.findByRole(UserRole.CLIENT)).thenReturn(users);
        when(userMapper.toDTO(user)).thenReturn(dtoWithPassword);

        List<UserDTO> result = clientService.getAllClients();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getPassword());
        verify(userRepository).findByRole(UserRole.CLIENT);
    }

    @Test
    void updateClient_Success() {
        UserDTO updateDTO = UserDTO.builder()
                .username("testuser")
                .name("Updated Name")
                .email("updated@example.com")
                .phone("9876543210")
                .address("456 New St")
                .role(UserRole.CLIENT)
                .build();

        when(userRepository.findByIdAndRole(1L, UserRole.CLIENT)).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateEntityFromDTO(updateDTO, user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(updateDTO);

        UserDTO result = clientService.updateClient(1L, updateDTO);

        assertNotNull(result);
        assertNull(result.getPassword());
        verify(userRepository).findByIdAndRole(1L, UserRole.CLIENT);
        verify(userMapper).updateEntityFromDTO(updateDTO, user);
        verify(userRepository).save(user);
    }

    @Test
    void updateClient_NotFound_ThrowsException() {
        when(userRepository.findByIdAndRole(anyLong(), eq(UserRole.CLIENT))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.updateClient(999L, userDTO);
        });
        verify(userRepository).findByIdAndRole(999L, UserRole.CLIENT);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteClient_Success() {
        when(userRepository.findByIdAndRole(1L, UserRole.CLIENT)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        clientService.deleteClient(1L);

        verify(userRepository).findByIdAndRole(1L, UserRole.CLIENT);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteClient_NotFound_ThrowsException() {
        when(userRepository.findByIdAndRole(anyLong(), eq(UserRole.CLIENT))).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.deleteClient(999L);
        });
        verify(userRepository).findByIdAndRole(999L, UserRole.CLIENT);
        verify(userRepository, never()).delete(any(User.class));
    }

}
