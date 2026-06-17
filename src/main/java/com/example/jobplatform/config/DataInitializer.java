  package com.example.jobplatform.config;

import com.example.jobplatform.entity.User;
import com.example.jobplatform.enums.Role;
import com.example.jobplatform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@jobplatform.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    public DataInitializer(JdbcTemplate jdbcTemplate,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        backfillNullRoles();
        seedAdmin();
    }

    // Legacy cleanup: default any rows missing a role to USER.
    private void backfillNullRoles() {
        try {
            int updated = jdbcTemplate.update("UPDATE users SET role = 'USER' WHERE role IS NULL");
            if (updated > 0) {
                log.info("Set default role=USER for {} existing users", updated);
            } else {
                log.info("No users required role defaulting");
            }
        } catch (Exception e) {
            log.warn("Could not run users role defaulting SQL: {}", e.getMessage());
        }
    }

    // Create a default ADMIN on startup if none exists. Idempotent — safe across restarts.
    private void seedAdmin() {
        try {
            if (userRepository.findByEmail(adminEmail).isPresent()) {
                log.info("Admin '{}' already exists — skipping seed", adminEmail);
                return;
            }
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            log.info("Seeded default admin user: {}", adminEmail);
        } catch (Exception e) {
            log.warn("Could not seed default admin: {}", e.getMessage());
        }
    }
}
