package com.kalibyte.tradex.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admins", indexes = {
    @Index(name = "idx_admin_code", columnList = "admin_code"),
    @Index(name = "idx_admin_email", columnList = "email"),
    @Index(name = "idx_admin_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "admin_code", nullable = false, unique = true)
    private String adminCode; // Auto-generated: ADMIN-0001, ADMIN-0002, etc.
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "phone", unique = true)
    private String phone;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash; // Bcrypt hashed
    
    @Column(name = "role", nullable = false)
    private String role; // SUPER_ADMIN, ADMIN, MODERATOR
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AdminStatus status = AdminStatus.ACTIVE;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @Column(name = "last_login_ip")
    private String lastLoginIp;
    
    @Column(name = "failed_attempts")
    @Builder.Default
    private Integer failedAttempts = 0;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    @Column(name = "force_password_change")
    @Builder.Default
    private Boolean forcePasswordChange = false;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum AdminStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        DELETED
    }
}
