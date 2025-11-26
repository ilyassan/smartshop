package com.smartshop.dto;

import com.smartshop.enums.CustomerTier;
import com.smartshop.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    @NotBlank(message = "Username is required")
    private String username;

    // Password should not be returned in responses
    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;

    private String name;

    @Email(message = "Email should be valid")
    private String email;

    private String phone;

    private String address;

    private CustomerTier loyaltyTier;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
