package com.smartshop.service;

import com.smartshop.dto.CouponDTO;

import java.util.List;

public interface CouponService {

    CouponDTO createCoupon(CouponDTO couponDTO);

    CouponDTO getCouponById(Long id);

    CouponDTO getCouponByCode(String code);

    List<CouponDTO> getAllCoupons();

    CouponDTO updateCoupon(Long id, CouponDTO couponDTO);

    void deleteCoupon(Long id);

    void useCoupon(String code);
}
