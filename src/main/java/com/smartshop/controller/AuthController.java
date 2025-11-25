package com.smartshop.controller;

import com.smartshop.dto.AuthResponse;
import com.smartshop.dto.LoginRequest;
import com.smartshop.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and create session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpSession session) {

        AuthResponse response = authService.login(loginRequest, session);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get currently authenticated user information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information retrieved"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<AuthResponse> getCurrentUser(HttpSession session) {
        AuthResponse response = authService.getCurrentUser(session);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and invalidate session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}
