package com.smartshop.util;

import com.smartshop.enums.CustomerTier;

import java.math.BigDecimal;

public class LoyaltyTierRules {

    // TIER ACQUISITION: À partir de X commandes OU Y cumulés (OR logic)
    private static final BigDecimal SILVER_SPENDING_THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal GOLD_SPENDING_THRESHOLD = new BigDecimal("5000");
    private static final BigDecimal PLATINUM_SPENDING_THRESHOLD = new BigDecimal("15000");

    private static final int SILVER_ORDERS_THRESHOLD = 3;
    private static final int GOLD_ORDERS_THRESHOLD = 10;
    private static final int PLATINUM_ORDERS_THRESHOLD = 20;

    public static CustomerTier calculateTier(BigDecimal totalSpending, int confirmedOrdersCount) {
        if (totalSpending == null) {
            totalSpending = BigDecimal.ZERO;
        }

        // PLATINUM: 20 commandes OU 15,000 cumulés
        if ((confirmedOrdersCount >= PLATINUM_ORDERS_THRESHOLD)
                || (totalSpending.compareTo(PLATINUM_SPENDING_THRESHOLD) >= 0)) {
            return CustomerTier.PLATINUM;
        }

        // GOLD: 10 commandes OU 5,000 cumulés
        if ((confirmedOrdersCount >= GOLD_ORDERS_THRESHOLD)
                || (totalSpending.compareTo(GOLD_SPENDING_THRESHOLD) >= 0)) {
            return CustomerTier.GOLD;
        }

        // SILVER: 3 commandes OU 1,000 cumulés
        if ((confirmedOrdersCount >= SILVER_ORDERS_THRESHOLD)
                || (totalSpending.compareTo(SILVER_SPENDING_THRESHOLD) >= 0)) {
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
