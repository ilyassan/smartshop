package com.smartshop.integration;

import com.smartshop.dto.CouponDTO;
import com.smartshop.entity.Coupon;
import com.smartshop.entity.User;
import com.smartshop.enums.UserRole;
import com.smartshop.repository.CouponRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.util.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CouponControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private Coupon testCoupon;
    private MockHttpSession adminSession;

    @BeforeEach
    void setUp() {
        couponRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .build();
        adminUser = userRepository.save(adminUser);

        testCoupon = Coupon.builder()
                .code("TEST10")
                .discountPercentage(new BigDecimal("10.00"))
                .isUsed(false)
                .build();
        testCoupon = couponRepository.save(testCoupon);

        adminSession = new MockHttpSession();
        adminSession.setAttribute("LOGGED_IN_USER", adminUser.getId());
        adminSession.setAttribute("userRole", "ADMIN");
    }

    @Test
    void createCoupon_Success() throws Exception {
        CouponDTO newCoupon = CouponDTO.builder()
                .code("SAVE20")
                .discountPercentage(new BigDecimal("20.00"))
                .isUsed(false)
                .build();

        mockMvc.perform(post("/coupons")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCoupon)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is("SAVE20")))
                .andExpect(jsonPath("$.discountPercentage", is(20.00)))
                .andExpect(jsonPath("$.isUsed", is(false)));
    }

    @Test
    void createCoupon_DuplicateCode_BadRequest() throws Exception {
        CouponDTO newCoupon = CouponDTO.builder()
                .code("TEST10")
                .discountPercentage(new BigDecimal("15.00"))
                .build();

        mockMvc.perform(post("/coupons")
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCoupon)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCouponById_Success() throws Exception {
        mockMvc.perform(get("/coupons/" + testCoupon.getId())
                        .session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCoupon.getId().intValue())))
                .andExpect(jsonPath("$.code", is("TEST10")))
                .andExpect(jsonPath("$.discountPercentage", is(10.00)));
    }

    @Test
    void getCouponById_NotFound() throws Exception {
        mockMvc.perform(get("/coupons/999")
                        .session(adminSession))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCouponByCode_Success() throws Exception {
        mockMvc.perform(get("/coupons/code/TEST10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("TEST10")))
                .andExpect(jsonPath("$.discountPercentage", is(10.00)));
    }

    @Test
    void getCouponByCode_NotFound() throws Exception {
        mockMvc.perform(get("/coupons/code/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCoupons_Success() throws Exception {
        mockMvc.perform(get("/coupons")
                        .session(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void updateCoupon_Success() throws Exception {
        CouponDTO updateDTO = CouponDTO.builder()
                .code("TEST10")
                .discountPercentage(new BigDecimal("15.00"))
                .isUsed(false)
                .build();

        mockMvc.perform(put("/coupons/" + testCoupon.getId())
                        .session(adminSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountPercentage", is(15.00)));
    }

    @Test
    void deleteCoupon_Success() throws Exception {
        mockMvc.perform(delete("/coupons/" + testCoupon.getId())
                        .session(adminSession))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/coupons/" + testCoupon.getId())
                        .session(adminSession))
                .andExpect(status().isNotFound());
    }
}
