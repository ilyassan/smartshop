package com.smartshop.config;

import com.smartshop.entity.User;
import com.smartshop.enums.UserRole;
import com.smartshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
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
                    .clientId(null)
                    .build();
            userRepository.save(client);
            log.info("Created CLIENT user: username=client1, password=client123");

            log.info("Users seeded successfully!");
        } else {
            log.info("Users already exist, skipping seed");
        }
    }
}
