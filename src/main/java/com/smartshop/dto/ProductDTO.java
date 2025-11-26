package com.smartshop.dto;

import jakarta.validation.constraints.NotBlank;
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
public class ProductDTO {

    private Long id;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "SKU is required")
    private String sku;

    private String description;

    @NotNull(message = "Unit price is required")
    private BigDecimal unitPrice;

    @Builder.Default
    private Integer stock = 0;

    private String category;

    @Builder.Default
    private Boolean deleted = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
