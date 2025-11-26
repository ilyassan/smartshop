package com.smartshop.controller;

import com.smartshop.entity.Coupon;
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
    public ResponseEntity<Coupon> createCoupon(@Valid @RequestBody Coupon coupon, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can create coupons");
        }

        Coupon createdCoupon = couponService.createCoupon(coupon);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCoupon);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Coupon> getCouponById(@PathVariable Long id) {
        Coupon coupon = couponService.getCouponById(id);
        return ResponseEntity.ok(coupon);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Coupon> getCouponByCode(@PathVariable String code) {
        Coupon coupon = couponService.getCouponByCode(code);
        return ResponseEntity.ok(coupon);
    }

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons(HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can view all coupons");
        }

        List<Coupon> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Coupon> updateCoupon(
            @PathVariable Long id,
            @RequestBody Coupon coupon,
            HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !userRole.equals("ADMIN")) {
            throw new UnauthorizedException("Only admins can update coupons");
        }

        Coupon updatedCoupon = couponService.updateCoupon(id, coupon);
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
