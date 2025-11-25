package com.smartshop.util;

import com.smartshop.enums.CustomerTier;

import java.math.BigDecimal;

public class LoyaltyTierRules {

    private static final BigDecimal SILVER_SPENDING_THRESHOLD = new BigDecimal("5000");
    private static final BigDecimal GOLD_SPENDING_THRESHOLD = new BigDecimal("15000");
    private static final BigDecimal PLATINUM_SPENDING_THRESHOLD = new BigDecimal("30000");

    private static final int SILVER_ORDERS_THRESHOLD = 5;
    private static final int GOLD_ORDERS_THRESHOLD = 15;
    private static final int PLATINUM_ORDERS_THRESHOLD = 30;

    public static CustomerTier calculateTier(BigDecimal totalSpending, int confirmedOrdersCount) {
        if (totalSpending == null) {
            totalSpending = BigDecimal.ZERO;
        }

        if (totalSpending.compareTo(PLATINUM_SPENDING_THRESHOLD) >= 0
                && confirmedOrdersCount >= PLATINUM_ORDERS_THRESHOLD) {
            return CustomerTier.PLATINUM;
        }

        if (totalSpending.compareTo(GOLD_SPENDING_THRESHOLD) >= 0
                && confirmedOrdersCount >= GOLD_ORDERS_THRESHOLD) {
            return CustomerTier.GOLD;
        }

        if (totalSpending.compareTo(SILVER_SPENDING_THRESHOLD) >= 0
                && confirmedOrdersCount >= SILVER_ORDERS_THRESHOLD) {
            return CustomerTier.SILVER;
        }

        return CustomerTier.BASIC;
    }

    public static boolean needsUpgrade(CustomerTier currentTier, BigDecimal totalSpending, int confirmedOrdersCount) {
        CustomerTier calculatedTier = calculateTier(totalSpending, confirmedOrdersCount);
        return calculatedTier.ordinal() > currentTier.ordinal();
    }

    public static CustomerTier getUpgradedTier(BigDecimal totalSpending, int confirmedOrdersCount) {
        return calculateTier(totalSpending, confirmedOrdersCount);
    }
}
