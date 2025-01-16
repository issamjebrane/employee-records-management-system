package com.example.erm.config;

import com.example.erm.entities.User;
import com.example.erm.entities.UserRole;
import com.example.erm.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Check if admin user exists
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPasswordHash(passwordEncoder.encode("admin123")); // Change this password
            adminUser.setRole(UserRole.ADMIN);
            adminUser.setCreatedAt(LocalDateTime.now());

            userRepository.save(adminUser);

            System.out.println("Admin user created successfully!");
        }
    }
}
