package com.smartshop.controller;

import com.smartshop.dto.CouponDTO;
import com.smartshop.exception.UnauthorizedException;
import com.smartshop.service.CouponService;
import jakarta.servlet.http.HttpSession;
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
    public ResponseEntity<CouponDTO> createCoupon(@Valid @RequestBody CouponDTO coupon, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can create coupons");
        }

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
    public ResponseEntity<List<CouponDTO>> getAllCoupons(HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can view all coupons");
        }

        List<CouponDTO> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouponDTO> updateCoupon(
            @PathVariable Long id,
            @Valid @RequestBody CouponDTO coupon,
            HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can update coupons");
        }

        CouponDTO updatedCoupon = couponService.updateCoupon(id, coupon);
        return ResponseEntity.ok(updatedCoupon);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can delete coupons");
        }

        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/use/{code}")
    public ResponseEntity<Void> useCoupon(@PathVariable String code, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can manually mark coupons as used");
        }

        couponService.useCoupon(code);
        return ResponseEntity.noContent().build();
    }
}
