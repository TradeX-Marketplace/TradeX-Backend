package com.kalibyte.tradex.trading.entity;

import com.kalibyte.tradex.commodity.entity.Commodity;
import com.kalibyte.tradex.commodity.entity.QualityGrade;
import com.kalibyte.tradex.party.entity.Party;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Supply Entity
 * 
 * Represents a supply lot posted by a seller/manufacturer
 * Tracks inventory with available and locked quantities (pessimistic locking)
 */
@Entity
@Table(name = "supplies", indexes = {
    @Index(name = "idx_supplies_comm_status", columnList = "commodity_id, status"),
    @Index(name = "idx_supplies_party", columnList = "party_id"),
    @Index(name = "idx_supplies_valid_till", columnList = "valid_till")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"party", "commodity", "qualityGrade"})
@EqualsAndHashCode(exclude = {"party", "commodity", "qualityGrade"})
public class Supply {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String supplyNumber;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "commodity_id", nullable = false)
    private Commodity commodity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quality_grade_id")
    private QualityGrade qualityGrade;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalQuantity;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal availableQuantity;
    
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal lockedQuantity = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal askingRate;
    
    @Column(nullable = false, length = 255)
    private String location;
    
    @Column(nullable = false, length = 50)
    private String originZone;
    
    @Column(length = 100)
    private String maskedLabel;
    
    @Column(name = "origin_manufacturer_id")
    private Long originManufacturerId;
    
    @Column(name = "origin_manufacturer_name", length = 255)
    private String originManufacturerName;
    
    @Column(columnDefinition = "TEXT")
    private String labSpecs;
    
    @Column(nullable = false)
    private LocalDate validTill;
    
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "AVAILABLE";
    
    @Column(length = 100)
    private String createdBy;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (supplyNumber == null) {
            supplyNumber = "SUP-" + System.currentTimeMillis();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
