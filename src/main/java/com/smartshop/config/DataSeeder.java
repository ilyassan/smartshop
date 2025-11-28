package com.smartshop.config;

import com.smartshop.entity.Coupon;
import com.smartshop.entity.User;
import com.smartshop.enums.UserRole;
import com.smartshop.repository.CouponRepository;
import com.smartshop.repository.UserRepository;
import com.smartshop.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedCoupons();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            log.info("Seeding initial users...");

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Created ADMIN user: username=admin, password=admin123");

            User client = User.builder()
                    .username("client1")
                    .password(passwordEncoder.encode("client123"))
                    .role(UserRole.CLIENT)
                    .build();
            userRepository.save(client);
            log.info("Created CLIENT user: username=client1, password=client123");

            log.info("Users seeded successfully!");
        } else {
            log.info("Users already exist, skipping seed");
        }
    }

    private void seedCoupons() {
        if (couponRepository.count() == 0) {
            log.info("Seeding initial coupons...");

            Coupon coupon1 = Coupon.builder()
                    .code("WELCOME10")
                    .discountPercentage(new BigDecimal("10.00"))
                    .build();
            couponRepository.save(coupon1);

            Coupon coupon2 = Coupon.builder()
                    .code("SAVE15")
                    .discountPercentage(new BigDecimal("15.00"))
                    .build();
            couponRepository.save(coupon2);

            Coupon coupon3 = Coupon.builder()
                    .code("MEGA20")
                    .discountPercentage(new BigDecimal("20.00"))
                    .build();
            couponRepository.save(coupon3);

            Coupon coupon4 = Coupon.builder()
                    .code("VIP25")
                    .discountPercentage(new BigDecimal("25.00"))
                    .build();
            couponRepository.save(coupon4);

            log.info("Coupons seeded successfully!");
        } else {
            log.info("Coupons already exist, skipping seed");
        }
    }
}
