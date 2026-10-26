package com.kalibyte.tradex.party.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "party_capabilities", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"party_id", "capability_type"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyCapability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "party_id", nullable = false)
    private Long partyId; // FK to parties
    
    @Column(name = "capability_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CapabilityType capabilityType;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CapabilityStatus status = CapabilityStatus.ACTIVE;
    
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
    
    public enum CapabilityType {
        BUYER,          // Can purchase commodities
        SELLER,         // Can supply commodities
        MANUFACTURER,   // Can manufacture commodities
        TRADER          // Can trade in commodities
    }
    
    public enum CapabilityStatus {
        ACTIVE,
        INACTIVE
    }
}
