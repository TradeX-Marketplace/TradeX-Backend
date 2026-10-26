package com.kalibyte.tradex.auth.service;

import com.kalibyte.tradex.auth.dto.request.AdminLoginRequest;
import com.kalibyte.tradex.auth.dto.response.AdminLoginResponse;
import com.kalibyte.tradex.auth.entity.Admin;
import com.kalibyte.tradex.auth.repository.AdminRepository;
import com.kalibyte.tradex.audit.entity.AuditLog;
import com.kalibyte.tradex.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminAuthService {
    
    private final AdminRepository adminRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final com.kalibyte.tradex.notification.config.WhatsAppConfig whatsAppConfig;
    
    @Value("${app.jwt.expiration:86400000}")
    private long jwtExpirationMs;
    
    /**
     * Admin login with email and password
     */
    public AdminLoginResponse loginAdmin(AdminLoginRequest request, String ipAddress) {
        log.info("Admin login attempt for email: {}", request.getEmail());
        
        // Find admin by email
        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Admin not found for email: {}", request.getEmail());
                    auditService.logFailedAction(
                            null,
                            AuditLog.AuditActionType.LOGIN_FAILED,
                            "Admin login failed - Email not found: " + request.getEmail(),
                            request.getEmail(),
                            AuditLog.ActorType.ADMIN,
                            ipAddress,
                            "Email not found"
                    );
                    return new RuntimeException("Invalid email or password");
                });
        
        // Check if admin is active
        if (!admin.getActive() || admin.getStatus() == Admin.AdminStatus.SUSPENDED) {
            log.warn("Admin account is inactive or suspended: {}", request.getEmail());
            auditService.logFailedAction(
                    null,
                    AuditLog.AuditActionType.LOGIN_FAILED,
                    "Admin login failed - Account inactive/suspended: " + request.getEmail(),
                    request.getEmail(),
                    AuditLog.ActorType.ADMIN,
                    ipAddress,
                    "Account inactive or suspended"
            );
            throw new RuntimeException("Account is inactive or suspended");
        }
        
        // Check if account is locked
        if (admin.getLockedUntil() != null && admin.getLockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("Admin account is locked: {}", request.getEmail());
            auditService.logFailedAction(
                    null,
                    AuditLog.AuditActionType.LOGIN_FAILED,
                    "Admin login failed - Account locked: " + request.getEmail(),
                    request.getEmail(),
                    AuditLog.ActorType.ADMIN,
                    ipAddress,
                    "Account is locked"
            );
            throw new RuntimeException("Account is locked. Please try again later.");
        }
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            log.warn("Invalid password for admin: {}", request.getEmail());
            
            // Increment failed attempts
            admin.setFailedAttempts(admin.getFailedAttempts() + 1);
            
            // Lock account if 5 failed attempts
            if (admin.getFailedAttempts() >= 5) {
                admin.setLockedUntil(LocalDateTime.now().plusMinutes(30));
                adminRepository.save(admin);
                
                log.error("Admin account locked due to multiple failed attempts: {}", request.getEmail());
                auditService.logFailedAction(
                        null,
                        AuditLog.AuditActionType.LOGIN_FAILED,
                        "Admin account locked - 5 failed login attempts: " + request.getEmail(),
                        request.getEmail(),
                        AuditLog.ActorType.ADMIN,
                        ipAddress,
                        "Account locked - too many failed attempts"
                );
                throw new RuntimeException("Account locked due to multiple failed login attempts. Please try again later.");
            }
            
            adminRepository.save(admin);
            
            auditService.logFailedAction(
                    null,
                    AuditLog.AuditActionType.LOGIN_FAILED,
                    "Admin login failed - Invalid password for: " + request.getEmail(),
                    request.getEmail(),
                    AuditLog.ActorType.ADMIN,
                    ipAddress,
                    "Invalid password"
            );
            throw new RuntimeException("Invalid email or password");
        }
        
        // Reset failed attempts on successful login
        admin.setFailedAttempts(0);
        admin.setLockedUntil(null);
        admin.setLastLogin(LocalDateTime.now());
        admin.setLastLoginIp(ipAddress);
        
        Admin updatedAdmin = adminRepository.save(admin);
        
        // Generate tokens
        String token = jwtTokenProvider.generateToken(
                admin.getId(),
                admin.getEmail(),
                admin.getName(),
                admin.getRole()
        );
        
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                admin.getId(),
                admin.getEmail()
        );
        
        // Create audit log
        auditService.logAction(
                null,
                AuditLog.AuditActionType.LOGIN_SUCCESS,
                "Admin login successful: " + request.getEmail(),
                String.valueOf(admin.getId()),
                AuditLog.ActorType.ADMIN,
                ipAddress
        );
        
        log.info("Admin login successful for: {}", request.getEmail());
        
        return AdminLoginResponse.fromAdmin(updatedAdmin, token, refreshToken, jwtExpirationMs);
    }
    
    /**
     * Admin logout
     */
    @Transactional
    public void logoutAdmin(Long adminId, String ipAddress) {
        log.info("Admin logout: {}", adminId);
        
        auditService.logAction(
                null,
                AuditLog.AuditActionType.LOGOUT,
                "Admin logout - Admin ID: " + adminId,
                String.valueOf(adminId),
                AuditLog.ActorType.ADMIN,
                ipAddress
        );
    }
    
    /**
     * Get admin by ID
     */
    public Admin getAdminById(Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with ID: " + adminId));
    }
    
    /**
     * Get admin by email
     */
    public Admin getAdminByEmail(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin not found with email: " + email));
    }
    
    /**
     * Change admin password
     */
    @Transactional
    public void changePassword(Long adminId, String oldPassword, String newPassword, String ipAddress) {
        Admin admin = getAdminById(adminId);
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, admin.getPasswordHash())) {
            auditService.logFailedAction(
                    null,
                    AuditLog.AuditActionType.PASSWORD_CHANGED,
                    "Password change failed - Invalid old password for admin: " + admin.getEmail(),
                    String.valueOf(adminId),
                    AuditLog.ActorType.ADMIN,
                    ipAddress,
                    "Invalid old password"
            );
            throw new RuntimeException("Old password is incorrect");
        }
        
        // Update password
        admin.setPasswordHash(passwordEncoder.encode(newPassword));
        admin.setForcePasswordChange(false);
        adminRepository.save(admin);
        
        auditService.logAction(
                null,
                AuditLog.AuditActionType.PASSWORD_CHANGED,
                "Admin password changed successfully for: " + admin.getEmail(),
                String.valueOf(adminId),
                AuditLog.ActorType.ADMIN,
                ipAddress
        );
        
        log.info("Password changed for admin: {}", admin.getEmail());
    }

    /**
     * Update admin profile, credentials & WhatsApp alert phone number
     */
    @Transactional
    public Admin updateProfile(Long adminId, String name, String email, String phone, String newPassword, String ipAddress) {
        Admin admin = (adminId != null) 
                ? adminRepository.findById(adminId).orElse(null)
                : null;
        if (admin == null) {
            admin = adminRepository.findByEmail("admin@123gmail.com")
                    .orElseGet(() -> adminRepository.findAll().stream().findFirst().orElse(null));
        }
        if (admin == null) {
            throw new RuntimeException("Admin profile not found");
        }

        if (name != null && !name.trim().isEmpty()) {
            admin.setName(name.trim());
        }
        if (email != null && !email.trim().isEmpty()) {
            admin.setEmail(email.trim());
        }
        if (phone != null && !phone.trim().isEmpty()) {
            admin.setPhone(phone.trim());
            // Also update WhatsAppConfig runtime alert recipient phone immediately
            whatsAppConfig.setAdminPhone(phone.trim());
        }
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            admin.setPasswordHash(passwordEncoder.encode(newPassword.trim()));
            admin.setForcePasswordChange(false);
        }

        Admin saved = adminRepository.save(admin);
        log.info("Admin profile updated successfully for: {} (alert phone: {})", saved.getEmail(), saved.getPhone());
        return saved;
    }
}
