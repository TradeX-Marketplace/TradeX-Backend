package com.kalibyte.tradex.party.entity;

import com.kalibyte.tradex.party.entity.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "parties", indexes = {
    @Index(name = "idx_party_code", columnList = "party_code"),
    @Index(name = "idx_party_email", columnList = "email"),
    @Index(name = "idx_party_phone", columnList = "phone"),
    @Index(name = "idx_party_status", columnList = "status"),
    @Index(name = "idx_party_registration_type", columnList = "registration_type"),
    @Index(name = "idx_party_is_deleted", columnList = "is_deleted")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "roles")
@EqualsAndHashCode(exclude = "roles")
public class Party {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String partyCode; // Auto-generated: PARTY-0001, PARTY-0002, etc.
    
    @Column(nullable = false)
    private String companyName;
    
    @Column(name = "gstin", unique = true)
    private String gstin; // GSTIN for India
    
    @Column(name = "pan")
    private String pan; // PAN for India
    
    @Column(name = "contact_person")
    private String contactPerson;
    
    @Column(name = "email", unique = true)
    private String email;
    
    @Column(name = "phone", unique = true)
    private String phone;
    
    @Column(name = "alternate_phone")
    private String alternatePhone;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "city")
    private String city;

    @Column(name = "taluka")
    private String taluka;

    @Column(name = "plant_location")
    private String plantLocation;
    
    @Column(name = "state")
    private String state;
    
    @Column(name = "country")
    private String country;
    
    @Column(name = "postal_code")
    private String postalCode;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "website")
    private String website;
    
    @Column(name = "bank_account")
    private String bankAccount;
    
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "ifsc_code")
    private String ifscCode;
    
    // ===== NEW: Registration Flow Fields (Phase 3) =====
    
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(50) DEFAULT 'ACTIVE'")
    @Builder.Default
    private String status = "ACTIVE"; // Using String mapped to PartyStatus enum
    
    @Column(name = "registration_type", nullable = false, columnDefinition = "VARCHAR(50) DEFAULT 'ADMIN_CREATED'")
    @Builder.Default
    private String registrationType = "ADMIN_CREATED"; // Using String for flexibility
    
    @Column(name = "can_login", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean canLogin = false; // Can party login? (ACTIVE status only)
    
    @Column(name = "approved_by")
    private Long approvedBy; // Admin ID who approved
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt; // When approved
    
    @Column(name = "rejected_by")
    private Long rejectedBy; // Admin ID who rejected
    
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt; // When rejected
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason; // Why rejected
    
    @Column(name = "changes_required", columnDefinition = "TEXT")
    private String changesRequired; // What changes are needed
    
    @Column(name = "changes_requested_by")
    private Long changesRequestedBy; // Admin ID who requested changes
    
    @Column(name = "changes_requested_at")
    private LocalDateTime changesRequestedAt; // When changes were requested
    
    @Column(name = "requires_approval", columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean requiresApproval = false; // Flag for workflow
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true; // For backward compatibility

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false; // Soft delete flag
    
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
        name = "party_roles",
        joinColumns = @JoinColumn(name = "party_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    
    @Column(name = "created_by")
    private String createdBy; // Admin ID or "SELF" if self-registered
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = "ACTIVE";
        }
        if (registrationType == null) {
            registrationType = "ADMIN_CREATED";
        }
        if (canLogin == null) {
            canLogin = false;
        }
        if (requiresApproval == null) {
            requiresApproval = false;
        }
        if (active == null) {
            active = true;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Retrieve all distinct contact phone numbers configured for this party,
     * including primary phone and comma-separated alternate numbers.
     */
    public Set<String> getAllContactPhones() {
        Set<String> phones = new java.util.LinkedHashSet<>();
        if (this.phone != null && !this.phone.trim().isEmpty()) {
            phones.add(this.phone.trim());
        }
        if (this.alternatePhone != null && !this.alternatePhone.trim().isEmpty()) {
            for (String p : this.alternatePhone.split(",")) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) {
                    phones.add(trimmed);
                }
            }
        }
        return phones;
    }

    public String getName() {
        return companyName;
    }

    public void setStatus(PartyStatus partyStatus) {
        this.status = partyStatus != null ? partyStatus.name() : null;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
