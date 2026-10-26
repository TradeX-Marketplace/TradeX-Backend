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
 * Demand Entity
 * 
 * Represents a buyer's procurement inquiry/demand for a commodity
 * Tracks fulfillment progress with quantity matched
 */
@Entity
@Table(name = "demands", indexes = {
    @Index(name = "idx_demands_comm_status", columnList = "commodity_id, status"),
    @Index(name = "idx_demands_party", columnList = "party_id"),
    @Index(name = "idx_demands_valid_till", columnList = "valid_till")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"party", "commodity", "qualityGrade"})
@EqualsAndHashCode(exclude = {"party", "commodity", "qualityGrade"})
public class Demand {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String demandNumber;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "commodity_id", nullable = false)
    private Commodity commodity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quality_grade_id")
    private QualityGrade qualityGrade;
    
    @Column(name = "required_quantity", nullable = false, precision = 12, scale = 2)
    private BigDecimal requiredQuantity;
    
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal fulfilledQuantity = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal targetRate;
    
    @Column(nullable = false, length = 255)
    private String destination;
    
    @Column(nullable = false, length = 50)
    private String destinationZone;
    
    @Column(length = 100)
    private String maskedLabel;
    
    @Column(nullable = false)
    private LocalDate validTill;
    
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "OPEN";
    
    @Column(length = 20)
    @Builder.Default
    private String visibility = "PUBLIC";
    
    @Column(length = 100)
    private String createdBy;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (demandNumber == null) {
            demandNumber = "DEM-" + System.currentTimeMillis();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
