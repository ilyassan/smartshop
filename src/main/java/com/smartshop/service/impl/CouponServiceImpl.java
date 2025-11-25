package com.smartshop.service.impl;

import com.smartshop.entity.Coupon;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.CouponRepository;
import com.smartshop.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    @Override
    public Coupon createCoupon(Coupon coupon) {
        if (couponRepository.existsByCode(coupon.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists");
        }

        Coupon savedCoupon = couponRepository.save(coupon);
        log.info("Created coupon with code: {}", savedCoupon.getCode());

        return savedCoupon;
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon getCouponById(Long id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with code: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Override
    public Coupon updateCoupon(Long id, Coupon coupon) {
        Coupon existingCoupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));

        if (coupon.getCode() != null && !coupon.getCode().equals(existingCoupon.getCode())) {
            if (couponRepository.existsByCode(coupon.getCode())) {
                throw new IllegalArgumentException("Coupon code already exists");
            }
            existingCoupon.setCode(coupon.getCode());
        }

        if (coupon.getDiscountPercentage() != null) {
            existingCoupon.setDiscountPercentage(coupon.getDiscountPercentage());
        }

        Coupon updatedCoupon = couponRepository.save(existingCoupon);
        log.info("Updated coupon with id: {}", updatedCoupon.getId());

        return updatedCoupon;
    }

    @Override
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));

        couponRepository.delete(coupon);
        log.info("Deleted coupon with id: {}", id);
    }

    @Override
    public void useCoupon(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with code: " + code));

        couponRepository.delete(coupon);
        log.info("Used and deleted coupon with code: {}", code);
    }
}
