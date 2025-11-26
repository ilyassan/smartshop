package com.smartshop.dto;

import com.smartshop.enums.PaymentMethod;
import com.smartshop.enums.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private Long id;

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @NotNull(message = "Payment number is required")
    private Integer paymentNumber;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    private LocalDate collectionDate;

    @NotBlank(message = "Reference is required")
    private String reference;

    private String bankName;

    private LocalDate dueDate;

    @NotNull(message = "Payment status is required")
    private PaymentStatus status;

    private LocalDateTime createdAt;
}
