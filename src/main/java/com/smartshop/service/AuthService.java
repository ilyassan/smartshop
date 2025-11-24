package com.smartshop.service;

import com.smartshop.dto.AuthResponse;
import com.smartshop.dto.LoginRequest;
import jakarta.servlet.http.HttpSession;

public interface AuthService {

    AuthResponse login(LoginRequest loginRequest, HttpSession session);

    AuthResponse getCurrentUser(HttpSession session);

    void logout(HttpSession session);
}
