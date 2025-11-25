package com.smartshop.dto;

import com.smartshop.enums.CustomerTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String address;
    private CustomerTier loyaltyTier;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
