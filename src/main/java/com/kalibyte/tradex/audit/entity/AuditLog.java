package com.kalibyte.tradex.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_party_id", columnList = "party_id"),
    @Index(name = "idx_audit_action_type", columnList = "action_type"),
    @Index(name = "idx_audit_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "party_id")
    private Long partyId; // Target party being audited
    
    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuditActionType actionType;
    
    @Column(name = "action_description", nullable = false)
    private String actionDescription; // Detailed description
    
    @Column(name = "actor_id")
    private String actorId; // User/Admin who performed action
    
    @Column(name = "actor_type")
    @Enumerated(EnumType.STRING)
    private ActorType actorType; // ADMIN, PARTY, SYSTEM
    
    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues; // JSON of old values
    
    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues; // JSON of new values
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AuditStatus status; // SUCCESS, FAILURE
    
    @Column(name = "error_message")
    private String errorMessage; // If failed
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum AuditActionType {
        // Party Actions
        PARTY_REGISTERED,
        PARTY_CREATED_BY_ADMIN,
        PARTY_APPROVED,
        PARTY_REJECTED,
        PARTY_CHANGES_REQUESTED,
        PARTY_UPDATED,
        PARTY_DEACTIVATED,
        PARTY_ACTIVATED,
        PARTY_RESUBMITTED,
        
        // Capability Actions
        CAPABILITY_ADDED,
        CAPABILITY_REMOVED,
        CAPABILITY_UPDATED,
        
        // Commodity Mapping
        COMMODITY_MAPPING_CREATED,
        COMMODITY_MAPPING_UPDATED,
        COMMODITY_MAPPING_DELETED,
        COMMODITY_MAPPING_BULK_UPDATE,
        
        // Authentication
        LOGIN_SUCCESS,
        LOGIN_FAILED,
        LOGOUT,
        PASSWORD_CHANGED,
        PASSWORD_RESET,
        OTP_GENERATED,
        OTP_VERIFIED,
        EMAIL_VERIFIED,
        PHONE_VERIFIED,
        
        // Admin Actions
        ADMIN_APPROVAL_DECISION,
        ADMIN_REJECTION_DECISION,
        ADMIN_CHANGE_REQUEST,
        COMMODITY_ADDED,
        COMMODITY_UPDATED,
        COMMODITY_DELETED,
        
        // System Actions
        SYSTEM_NOTIFICATION_SENT,
        SYSTEM_ERROR,
        DATA_EXPORT,
        BULK_OPERATION
    }
    
    public enum ActorType {
        ADMIN,
        PARTY,
        SYSTEM
    }
    
    public enum AuditStatus {
        SUCCESS,
        FAILURE
    }
}
