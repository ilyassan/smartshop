package com.smartshop.service;

import com.smartshop.entity.Coupon;

import java.util.List;

public interface CouponService {

    Coupon createCoupon(Coupon coupon);

    Coupon getCouponById(Long id);

    Coupon getCouponByCode(String code);

    List<Coupon> getAllCoupons();

    Coupon updateCoupon(Long id, Coupon coupon);

    void deleteCoupon(Long id);

    void useCoupon(String code);
}
