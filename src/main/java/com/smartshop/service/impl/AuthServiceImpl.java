package com.smartshop.service.impl;

import com.smartshop.dto.AuthResponse;
import com.smartshop.dto.LoginRequest;
import com.smartshop.dto.UserDTO;
import com.smartshop.entity.User;
import com.smartshop.exception.UnauthorizedException;
import com.smartshop.mapper.UserMapper;
import com.smartshop.repository.UserRepository;
import com.smartshop.service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final String SESSION_USER_KEY = "LOGGED_IN_USER";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public AuthResponse login(LoginRequest loginRequest, HttpSession session) {
        log.debug("Attempting login for username: {}", loginRequest.getUsername());

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        session.setAttribute(SESSION_USER_KEY, user.getId());
        session.setAttribute("userRole", user.getRole().name());
        log.info("User {} logged in successfully with role {}", user.getUsername(), user.getRole());

        // Convert to DTO and ensure password is not returned
        UserDTO userDTO = userMapper.toDTO(user);
        userDTO.setPassword(null);

        return AuthResponse.builder()
                .id(userDTO.getId())
                .username(userDTO.getUsername())
                .role(userDTO.getRole())
                .message("Login successful")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_KEY);

        if (userId == null) {
            throw new UnauthorizedException("Not authenticated. Please login first");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Session invalid. Please login again"));

        // Convert to DTO and ensure password is not returned
        UserDTO userDTO = userMapper.toDTO(user);
        userDTO.setPassword(null);

        return AuthResponse.builder()
                .id(userDTO.getId())
                .username(userDTO.getUsername())
                .role(userDTO.getRole())
                .build();
    }

    @Override
    public void logout(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_KEY);

        if (userId != null) {
            log.info("User with ID {} logged out", userId);
        }

        session.invalidate();
    }
}
