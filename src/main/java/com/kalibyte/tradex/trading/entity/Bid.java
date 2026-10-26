package com.kalibyte.tradex.trading.entity;

import com.kalibyte.tradex.commodity.entity.Commodity;
import com.kalibyte.tradex.party.entity.Party;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bid Entity
 * 
 * Represents an anonymous bid placed by a buyer on a supply lot
 * Supports counter-offers and multi-round bidding
 */
@Entity
@Table(name = "bids", indexes = {
    @Index(name = "idx_bids_supply", columnList = "supply_id"),
    @Index(name = "idx_bids_demand", columnList = "demand_id"),
    @Index(name = "idx_bids_party", columnList = "party_id"),
    @Index(name = "idx_bids_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"commodity", "party", "supply", "demand"})
@EqualsAndHashCode(exclude = {"commodity", "party", "supply", "demand"})
public class Bid {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String bidNumber;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "commodity_id", nullable = false)
    private Commodity commodity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_id")
    private Supply supply;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demand_id")
    private Demand demand;
    
    @Column(nullable = false, length = 20)
    private String bidType; // BUY or SELL
    
    @Column(precision = 12, scale = 2)
    private BigDecimal marketRate;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal bidRate;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;
    
    @Column(nullable = false, length = 255)
    private String destination;
    
    @Column(length = 50)
    private String maskedBidderId;
    
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal counterRate;
    
    @Column(columnDefinition = "TEXT")
    private String counterRemarks;
    
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING";
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (bidNumber == null) {
            bidNumber = "BID-" + System.currentTimeMillis();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
