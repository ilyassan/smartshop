package com.smartshop.service;

import com.smartshop.entity.User;

public interface LoyaltyTierService {

    void checkAndUpgradeTier(Long userId);

    User upgradeTierIfEligible(Long userId);
}
