package com.kalibyte.tradex.trading.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import com.kalibyte.tradex.party.entity.Party;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Contract Entity
 * 
 * Represents a digital legal contract issued after deal lock
 * Stored as JSONB for flexible terms and conditions
 */
@Entity
@Table(name = "contracts", indexes = {
    @Index(name = "idx_contracts_deal", columnList = "deal_id"),
    @Index(name = "idx_contracts_unsigned", columnList = "status"),
    @Index(name = "idx_contracts_completed", columnList = "issued_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "deal")
@EqualsAndHashCode(exclude = "deal")
public class Contract {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deal_id", nullable = false, unique = true)
    private Deal deal;
    
    // Party Details
    @Column(nullable = false, length = 255)
    private String sellerName;
    
    @Column(nullable = false, length = 15)
    private String sellerGstin;
    
    @Column(columnDefinition = "TEXT")
    private String sellerAddress;
    
    @Column(nullable = false, length = 255)
    private String buyerName;
    
    @Column(nullable = false, length = 15)
    private String buyerGstin;
    
    @Column(columnDefinition = "TEXT")
    private String buyerAddress;
    
    // Contract Details
    @Column(unique = true, length = 50)
    private String contractNumber;
    
    @Column(nullable = false, length = 255)
    private String commodityName;
    
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity;
    
    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal rate;
    
    @Column(name = "total_amount", precision = 18, scale = 4, insertable = false, updatable = false)
    private BigDecimal totalAmount;
    
    // Terms
    @Column(name = "terms_conditions", columnDefinition = "TEXT")
    private String termsConditions;
    
    // Dispatch
    @Column(name = "dispatch_deadline")
    private LocalDateTime dispatchDeadline;
    
    @Column(name = "delivery_zone", length = 50)
    private String deliveryZone;
    
    // Status
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "ISSUED";
    
    // Signatures
    @Column(name = "seller_signed_at")
    private LocalDateTime sellerSignedAt;
    
    @Column(name = "buyer_signed_at")
    private LocalDateTime buyerSignedAt;
    
    // Audit
    @Column(nullable = false, name = "issued_at", updatable = false)
    @Builder.Default
    private LocalDateTime issuedAt = LocalDateTime.now();
    
    @Column(nullable = false, name = "created_by")
    private Long createdByPartyId;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", insertable = false, updatable = false)
    private Party createdBy;
    
    @PrePersist
    protected void onCreate() {
        issuedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getTotalAmount() {
        if (totalAmount != null) {
            return totalAmount;
        }
        if (quantity != null && rate != null) {
            return quantity.multiply(rate);
        }
        return null;
    }
}
