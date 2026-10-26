package com.kalibyte.tradex.auth.config;

import com.kalibyte.tradex.auth.entity.Admin;
import com.kalibyte.tradex.auth.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements ApplicationRunner {
    
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("Initializing default admin user...");
        
        try {
            // 1. Initialize / ensure admin@123gmail.com
            Admin admin1 = adminRepository.findByEmail("admin@123gmail.com").orElse(null);
            if (admin1 != null) {
                if (!passwordEncoder.matches("Admin@123", admin1.getPasswordHash())) {
                    admin1.setPasswordHash(passwordEncoder.encode("Admin@123"));
                    log.info("admin@123gmail.com password hash updated");
                }
                admin1.setFailedAttempts(0);
                admin1.setLockedUntil(null);
                admin1.setActive(true);
                admin1.setStatus(Admin.AdminStatus.ACTIVE);
                adminRepository.save(admin1);
                log.info("admin@123gmail.com verified and unlocked");
            } else {
                Admin newAdmin1 = Admin.builder()
                        .adminCode("ADMIN-0001")
                        .name("Super Admin")
                        .email("admin@123gmail.com")
                        .phone("+91-0000000000")
                        .passwordHash(passwordEncoder.encode("Admin@123"))
                        .role("SUPER_ADMIN")
                        .status(Admin.AdminStatus.ACTIVE)
                        .active(true)
                        .failedAttempts(0)
                        .forcePasswordChange(false)
                        .build();
                adminRepository.save(newAdmin1);
                log.info("Created default admin: admin@123gmail.com / Admin@123");
            }

            // 2. Initialize / ensure admin@tradex.com (from specification)
            Admin admin2 = adminRepository.findByEmail("admin@tradex.com").orElse(null);
            if (admin2 != null) {
                if (!passwordEncoder.matches("SecurePassword123", admin2.getPasswordHash())) {
                    admin2.setPasswordHash(passwordEncoder.encode("SecurePassword123"));
                    log.info("admin@tradex.com password hash updated");
                }
                admin2.setFailedAttempts(0);
                admin2.setLockedUntil(null);
                admin2.setActive(true);
                admin2.setStatus(Admin.AdminStatus.ACTIVE);
                adminRepository.save(admin2);
                log.info("admin@tradex.com verified and unlocked");
            } else {
                Admin newAdmin2 = Admin.builder()
                        .adminCode("ADMIN-0002")
                        .name("TradeX Admin")
                        .email("admin@tradex.com")
                        .phone("+91-9999999999")
                        .passwordHash(passwordEncoder.encode("SecurePassword123"))
                        .role("SUPER_ADMIN")
                        .status(Admin.AdminStatus.ACTIVE)
                        .active(true)
                        .failedAttempts(0)
                        .forcePasswordChange(false)
                        .build();
                adminRepository.save(newAdmin2);
                log.info("Created default admin: admin@tradex.com / SecurePassword123");
            }
        } catch (Exception e) {
            log.error("Error initializing default admins", e);
        }
    }
}
