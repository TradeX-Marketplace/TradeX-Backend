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
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Deal Entity
 * 
 * Represents a matched deal between buyer and seller
 * Status transitions: MATCHED -> LOCKED -> CONTRACT_ISSUED
 * Lock status manages pessimistic inventory locking
 */
@Entity
@Table(name = "deals", indexes = {
    @Index(name = "idx_deals_status", columnList = "status"),
    @Index(name = "idx_deals_parties", columnList = "buyer_party_id, seller_party_id"),
    @Index(name = "idx_deals_supply", columnList = "supply_id"),
    @Index(name = "idx_deals_lock_status", columnList = "lock_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"supply", "demand", "commodity", "buyerParty", "sellerParty"})
@EqualsAndHashCode(exclude = {"supply", "demand", "commodity", "buyerParty", "sellerParty"})
public class Deal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String dealNumber;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negotiation_id")
    private Negotiation negotiation;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id")
    private Bid bid;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supply_id", nullable = false)
    private Supply supply;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demand_id")
    private Demand demand;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "commodity_id", nullable = false)
    private Commodity commodity;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "buyer_party_id", nullable = false)
    private Party buyerParty;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_party_id", nullable = false)
    private Party sellerParty;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;
    
    @Column(name = "rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal rate;
    
    @Column(precision = 14, scale = 2)
    private BigDecimal totalValue;
    
    @Column(length = 255)
    private String destination;
    
    @Column
    private LocalDate dealDate;
    
    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "MATCHED";
    
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String lockStatus = "UNLOCKED";
    
    @Column
    private LocalDateTime lockedAt;
    
    @Column(length = 100)
    private String lockedBy;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (dealNumber == null) {
            dealNumber = "DEA-" + System.currentTimeMillis();
        }
        if (dealDate == null) {
            dealDate = LocalDate.now();
        }
        if (totalValue == null && quantity != null && rate != null) {
            totalValue = quantity.multiply(rate);
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        if (totalValue == null && quantity != null && rate != null) {
            totalValue = quantity.multiply(rate);
        }
        updatedAt = LocalDateTime.now();
    }
}
