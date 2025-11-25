package com.smartshop.util;

import com.smartshop.enums.CustomerTier;

import java.math.BigDecimal;

public class CustomerTierDiscount {

    private static final BigDecimal SILVER_THRESHOLD = new BigDecimal("500");
    private static final BigDecimal SILVER_DISCOUNT = new BigDecimal("0.05"); // 5%

    private static final BigDecimal GOLD_THRESHOLD = new BigDecimal("800");
    private static final BigDecimal GOLD_DISCOUNT = new BigDecimal("0.10"); // 10%

    private static final BigDecimal PLATINUM_THRESHOLD = new BigDecimal("1200");
    private static final BigDecimal PLATINUM_DISCOUNT = new BigDecimal("0.15"); // 15%

    public static BigDecimal calculateLoyaltyDiscount(CustomerTier tier, BigDecimal subtotal) {
        if (tier == null || subtotal == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;

        switch (tier) {
            case BASIC:
                discount = BigDecimal.ZERO;
                break;
            case SILVER:
                if (subtotal.compareTo(SILVER_THRESHOLD) >= 0) {
                    discount = SILVER_DISCOUNT;
                }
                break;
            case GOLD:
                if (subtotal.compareTo(GOLD_THRESHOLD) >= 0) {
                    discount = GOLD_DISCOUNT;
                }
                break;
            case PLATINUM:
                if (subtotal.compareTo(PLATINUM_THRESHOLD) >= 0) {
                    discount = PLATINUM_DISCOUNT;
                }
                break;
        }

        return discount;
    }

    public static BigDecimal applyLoyaltyDiscount(CustomerTier tier, BigDecimal subtotal) {
        BigDecimal discountRate = calculateLoyaltyDiscount(tier, subtotal);
        return subtotal.multiply(discountRate);
    }
}
