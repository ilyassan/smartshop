package com.smartshop.controller;

import com.smartshop.annotation.RequireRole;
import com.smartshop.dto.CouponDTO;
import com.smartshop.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @RequireRole("ADMIN")
    public ResponseEntity<CouponDTO> createCoupon(@Valid @RequestBody CouponDTO coupon) {
        CouponDTO createdCoupon = couponService.createCoupon(coupon);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCoupon);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponDTO> getCouponById(@PathVariable Long id) {
        CouponDTO coupon = couponService.getCouponById(id);
        return ResponseEntity.ok(coupon);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<CouponDTO> getCouponByCode(@PathVariable String code) {
        CouponDTO coupon = couponService.getCouponByCode(code);
        return ResponseEntity.ok(coupon);
    }

    @GetMapping
    @RequireRole("ADMIN")
    public ResponseEntity<List<CouponDTO>> getAllCoupons() {
        List<CouponDTO> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }

    @PutMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<CouponDTO> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponDTO coupon) {
        CouponDTO updatedCoupon = couponService.updateCoupon(id, coupon);
        return ResponseEntity.ok(updatedCoupon);
    }

    @DeleteMapping("/{id}")
    @RequireRole("ADMIN")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/use/{code}")
    @RequireRole("ADMIN")
    public ResponseEntity<Void> useCoupon(@PathVariable String code) {
        couponService.useCoupon(code);
        return ResponseEntity.noContent().build();
    }
}
