package com.smartshop.service.impl;

import com.smartshop.dto.CouponDTO;
import com.smartshop.entity.Coupon;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.mapper.CouponMapper;
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
    private final CouponMapper couponMapper;

    @Override
    public CouponDTO createCoupon(CouponDTO couponDTO) {
        if (couponRepository.existsByCode(couponDTO.getCode())) {
            throw new IllegalArgumentException("Coupon code already exists");
        }

        Coupon coupon = couponMapper.toEntity(couponDTO);
        Coupon savedCoupon = couponRepository.save(coupon);
        log.info("Created coupon with code: {}", savedCoupon.getCode());

        return couponMapper.toDTO(savedCoupon);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponDTO getCouponById(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));
        return couponMapper.toDTO(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponDTO getCouponByCode(String code) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with code: " + code));
        return couponMapper.toDTO(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponDTO> getAllCoupons() {
        List<Coupon> coupons = couponRepository.findAll();
        return couponMapper.toDTOList(coupons);
    }

    @Override
    public CouponDTO updateCoupon(Long id, CouponDTO couponDTO) {
        Coupon existingCoupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id));

        if (couponDTO.getCode() != null && !couponDTO.getCode().equals(existingCoupon.getCode())) {
            if (couponRepository.existsByCode(couponDTO.getCode())) {
                throw new IllegalArgumentException("Coupon code already exists");
            }
        }

        couponMapper.updateEntityFromDTO(couponDTO, existingCoupon);
        Coupon updatedCoupon = couponRepository.save(existingCoupon);
        log.info("Updated coupon with id: {}", updatedCoupon.getId());

        return couponMapper.toDTO(updatedCoupon);
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
