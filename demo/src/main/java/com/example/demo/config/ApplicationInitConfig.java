package com.example.demo.config;

import java.util.Set;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.User.Role;
import com.example.demo.entity.User.User;
import com.example.demo.repository.User.RoleRepository;
import com.example.demo.repository.User.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {
    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            Role adminRole = roleRepository.findByRoleName("Admin").orElseGet(() -> {
                Role newrole = new Role();
                newrole.setRoleName("Admin");
                return roleRepository.save(newrole);
            });

            if (userRepository.findByName("admin").isEmpty()) {
                User admin = User.builder()
                        .name("admin")
                        .email("admin@example.com")
                        .passwordHash(passwordEncoder.encode("admin")) // Mật khẩu mã hóa
                        .phone("0123456789")
                        .role(adminRole) // Gán quyền ADMIN
                        .build();

                userRepository.save(admin);
                log.warn("admin user has been created with default password: admin, please change it");
            }
        };
    }
}
