package com.kalibyte.tradex.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_credentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthCredential {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "party_id", nullable = false, unique = true)
    private Long partyId; // Reference to Party
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false, unique = true)
    private String phone;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // Bcrypt hashed
    
    @Column(name = "auth_status")
    @Enumerated(EnumType.STRING)
    private AuthStatus status; // ACTIVE, INACTIVE, LOCKED, PENDING_VERIFICATION
    
    @Column(name = "login_enabled")
    private Boolean loginEnabled; // Can user login?
    
    @Column(name = "otp")
    private String otp; // For OTP-based login
    
    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;
    
    @Column(name = "email_verified")
    private Boolean emailVerified; // Email verification status
    
    @Column(name = "phone_verified")
    private Boolean phoneVerified; // Phone verification status
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "last_login_ip")
    private String lastLoginIp;
    
    @Column(name = "failed_attempts")
    private Integer failedAttempts; // For brute force protection
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil; // Account lock expiry
    
    @Column(name = "force_password_change")
    private Boolean forcePasswordChange; // Force user to change password on next login
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = AuthStatus.INACTIVE;
        loginEnabled = false;
        emailVerified = false;
        phoneVerified = false;
        failedAttempts = 0;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum AuthStatus {
        ACTIVE,                    // Ready to login
        INACTIVE,                  // Cannot login (pending approval)
        LOCKED,                    // Account locked due to failed attempts
        PENDING_VERIFICATION,      // Waiting for email/phone verification
        SUSPENDED,                 // Suspended by admin
        DELETED                    // Soft deleted
    }
}
