package com.kalibyte.tradex.party.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "party_commodity_mapping", uniqueConstraints = {
    @UniqueConstraint(name = "uq_party_commodity_capability", columnNames = {"party_id", "commodity_id", "capability_type"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyCommodityMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "party_id", nullable = false)
    private Long partyId;

    @Column(name = "commodity_id", nullable = false)
    private Long commodityId;

    @Column(name = "capability_type", nullable = false, length = 20)
    @Builder.Default
    private String capabilityType = "BOTH"; // BUY, SELL, BOTH

    @Column(name = "quality_grade_id")
    private Long qualityGradeId;

    @Column(name = "notification_enabled", nullable = false)
    @Builder.Default
    private Boolean notificationEnabled = true;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, ARCHIVED, DELETED

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (capabilityType == null) {
            capabilityType = "BOTH";
        }
        if (notificationEnabled == null) {
            notificationEnabled = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
