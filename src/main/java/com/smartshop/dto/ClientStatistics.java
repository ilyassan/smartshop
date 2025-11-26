package com.smartshop.dto;

import com.smartshop.enums.CustomerTier;
import com.smartshop.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientStatistics {

    private Long clientId;
    private String clientName;
    private String email;
    private CustomerTier loyaltyTier;

    private Integer totalOrders;
    private BigDecimal totalSpent;
    private BigDecimal totalRemaining;

    private LocalDateTime firstOrderDate;
    private LocalDateTime lastOrderDate;

    private Map<OrderStatus, Integer> ordersByStatus;

    private List<OrderSummary> recentOrders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummary {
        private Long orderId;
        private LocalDateTime orderDate;
        private BigDecimal totalTTC;
        private OrderStatus status;
    }
}
