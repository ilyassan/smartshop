package com.smartshop.service.impl;

import com.smartshop.dto.AuthResponse;
import com.smartshop.dto.LoginRequest;
import com.smartshop.entity.User;
import com.smartshop.exception.UnauthorizedException;
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

    @Override
    public AuthResponse login(LoginRequest loginRequest, HttpSession session) {
        log.debug("Attempting login for username: {}", loginRequest.getUsername());

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        session.setAttribute(SESSION_USER_KEY, user.getId());
        log.info("User {} logged in successfully with role {}", user.getUsername(), user.getRole());

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .clientId(user.getClientId())
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

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .clientId(user.getClientId())
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
