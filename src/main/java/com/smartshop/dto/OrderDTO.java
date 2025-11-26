package com.smartshop.dto;

import com.smartshop.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
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
public class OrderDTO {

    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Order date is required")
    private LocalDateTime orderDate;

    @NotNull(message = "Order status is required")
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @NotNull(message = "Subtotal HT is required")
    private BigDecimal subtotalHT;

    @NotNull(message = "Total TTC is required")
    private BigDecimal totalTTC;

    @NotNull(message = "Remaining amount is required")
    private BigDecimal remainingAmount;

    private Long couponId;

    // Nested DTO for output
    private CouponDTO coupon;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
