package com.smartshop.service;

import com.smartshop.dto.CouponDTO;
import com.smartshop.entity.Coupon;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.mapper.CouponMapper;
import com.smartshop.repository.CouponRepository;
import com.smartshop.service.impl.CouponServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponMapper couponMapper;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Coupon coupon;
    private CouponDTO couponDTO;

    @BeforeEach
    void setUp() {
        coupon = Coupon.builder()
                .id(1L)
                .code("TEST10")
                .discountPercentage(new BigDecimal("10.00"))
                .isUsed(false)
                .build();

        couponDTO = CouponDTO.builder()
                .id(1L)
                .code("TEST10")
                .discountPercentage(new BigDecimal("10.00"))
                .isUsed(false)
                .build();
    }

    @Test
    void createCoupon_Success() {
        when(couponRepository.existsByCode(couponDTO.getCode())).thenReturn(false);
        when(couponMapper.toEntity(couponDTO)).thenReturn(coupon);
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);
        when(couponMapper.toDTO(coupon)).thenReturn(couponDTO);

        CouponDTO result = couponService.createCoupon(couponDTO);

        assertNotNull(result);
        assertEquals(couponDTO.getCode(), result.getCode());
        assertEquals(couponDTO.getDiscountPercentage(), result.getDiscountPercentage());
        assertFalse(result.getIsUsed());
        verify(couponRepository).existsByCode(couponDTO.getCode());
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void createCoupon_CodeAlreadyExists_ThrowsException() {
        when(couponRepository.existsByCode(couponDTO.getCode())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            couponService.createCoupon(couponDTO);
        });
        verify(couponRepository).existsByCode(couponDTO.getCode());
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void getCouponById_Success() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponMapper.toDTO(coupon)).thenReturn(couponDTO);

        CouponDTO result = couponService.getCouponById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TEST10", result.getCode());
        verify(couponRepository).findById(1L);
    }

    @Test
    void getCouponById_NotFound_ThrowsException() {
        when(couponRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            couponService.getCouponById(999L);
        });
        verify(couponRepository).findById(999L);
    }

    @Test
    void getCouponByCode_Success() {
        when(couponRepository.findByCode("TEST10")).thenReturn(Optional.of(coupon));
        when(couponMapper.toDTO(coupon)).thenReturn(couponDTO);

        CouponDTO result = couponService.getCouponByCode("TEST10");

        assertNotNull(result);
        assertEquals("TEST10", result.getCode());
        verify(couponRepository).findByCode("TEST10");
    }

    @Test
    void getCouponByCode_NotFound_ThrowsException() {
        when(couponRepository.findByCode(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            couponService.getCouponByCode("INVALID");
        });
        verify(couponRepository).findByCode("INVALID");
    }

    @Test
    void getAllCoupons_Success() {
        List<Coupon> coupons = Arrays.asList(coupon);
        when(couponRepository.findAll()).thenReturn(coupons);
        when(couponMapper.toDTOList(coupons)).thenReturn(Arrays.asList(couponDTO));

        List<CouponDTO> result = couponService.getAllCoupons();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TEST10", result.get(0).getCode());
        verify(couponRepository).findAll();
    }

    @Test
    void updateCoupon_Success() {
        CouponDTO updateDTO = CouponDTO.builder()
                .code("TEST10")
                .discountPercentage(new BigDecimal("15.00"))
                .isUsed(false)
                .build();

        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        doNothing().when(couponMapper).updateEntityFromDTO(updateDTO, coupon);
        when(couponRepository.save(coupon)).thenReturn(coupon);
        when(couponMapper.toDTO(coupon)).thenReturn(updateDTO);

        CouponDTO result = couponService.updateCoupon(1L, updateDTO);

        assertNotNull(result);
        verify(couponRepository).findById(1L);
        verify(couponMapper).updateEntityFromDTO(updateDTO, coupon);
        verify(couponRepository).save(coupon);
    }

    @Test
    void updateCoupon_NotFound_ThrowsException() {
        when(couponRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            couponService.updateCoupon(999L, couponDTO);
        });
        verify(couponRepository).findById(999L);
        verify(couponRepository, never()).save(any(Coupon.class));
    }

    @Test
    void deleteCoupon_Success() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        doNothing().when(couponRepository).delete(coupon);

        couponService.deleteCoupon(1L);

        verify(couponRepository).findById(1L);
        verify(couponRepository).delete(coupon);
    }

    @Test
    void deleteCoupon_NotFound_ThrowsException() {
        when(couponRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            couponService.deleteCoupon(999L);
        });
        verify(couponRepository).findById(999L);
        verify(couponRepository, never()).delete(any(Coupon.class));
    }

    // Test: useCoupon - Successful coupon deletion
    @Test
    void useCoupon_Success() {
        when(couponRepository.findByCode("TEST10")).thenReturn(Optional.of(coupon));
        doNothing().when(couponRepository).delete(coupon);

        couponService.useCoupon("TEST10");

        verify(couponRepository).findByCode("TEST10");
        verify(couponRepository).delete(coupon);
    }

    // Test: useCoupon - Coupon not found (lambda exception coverage)
    @Test
    void useCoupon_CouponNotFound_ThrowsException() {
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            couponService.useCoupon("INVALID");
        });
        verify(couponRepository).findByCode("INVALID");
        verify(couponRepository, never()).delete(any(Coupon.class));
    }

    // Test: useCoupon - Deletes the correct coupon with exact code match
    @Test
    void useCoupon_DeletesCorrectCoupon() {
        Coupon coupon2 = Coupon.builder()
                .id(2L)
                .code("PROMO20")
                .discountPercentage(new BigDecimal("20.00"))
                .isUsed(false)
                .build();

        when(couponRepository.findByCode("PROMO20")).thenReturn(Optional.of(coupon2));
        doNothing().when(couponRepository).delete(coupon2);

        couponService.useCoupon("PROMO20");

        verify(couponRepository).findByCode("PROMO20");
        verify(couponRepository).delete(coupon2);
        // Verify it's called exactly once (not multiple times)
        verify(couponRepository, times(1)).delete(coupon2);
    }

    // Test: useCoupon - Case sensitive code lookup
    @Test
    void useCoupon_CaseSensitiveCodeLookup() {
        when(couponRepository.findByCode("test10")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            couponService.useCoupon("test10"); // lowercase should not match "TEST10"
        });
        verify(couponRepository).findByCode("test10");
        verify(couponRepository, never()).delete(any(Coupon.class));
    }

    // Test: useCoupon - Already used coupon can still be deleted
    @Test
    void useCoupon_AlreadyUsedCoupon() {
        Coupon usedCoupon = Coupon.builder()
                .id(1L)
                .code("TEST10")
                .discountPercentage(new BigDecimal("10.00"))
                .isUsed(true) // Already marked as used
                .build();

        when(couponRepository.findByCode("TEST10")).thenReturn(Optional.of(usedCoupon));
        doNothing().when(couponRepository).delete(usedCoupon);

        couponService.useCoupon("TEST10");

        // Verify deletion still happens for already-used coupons
        verify(couponRepository).findByCode("TEST10");
        verify(couponRepository).delete(usedCoupon);
    }

    // Test: useCoupon - Special characters in coupon code
    @Test
    void useCoupon_SpecialCharactersInCode() {
        Coupon specialCoupon = Coupon.builder()
                .id(3L)
                .code("PROMO-SUMMER-2024")
                .discountPercentage(new BigDecimal("25.00"))
                .isUsed(false)
                .build();

        when(couponRepository.findByCode("PROMO-SUMMER-2024")).thenReturn(Optional.of(specialCoupon));
        doNothing().when(couponRepository).delete(specialCoupon);

        couponService.useCoupon("PROMO-SUMMER-2024");

        verify(couponRepository).findByCode("PROMO-SUMMER-2024");
        verify(couponRepository).delete(specialCoupon);
    }

}
