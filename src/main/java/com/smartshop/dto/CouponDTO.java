package com.smartshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDTO {

    private Long id;

    @NotBlank(message = "Coupon code is required")
    @Pattern(regexp = "PROMO-[A-Z0-9]{4}", message = "Coupon code must follow format: PROMO-XXXX (where X is alphanumeric)")
    private String code;

    @NotNull(message = "Discount percentage is required")
    private BigDecimal discountPercentage;

    @Builder.Default
    private Boolean isUsed = false;

    private LocalDateTime createdAt;
}
