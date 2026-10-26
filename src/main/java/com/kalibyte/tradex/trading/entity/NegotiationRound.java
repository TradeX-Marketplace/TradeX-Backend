package com.kalibyte.tradex.trading.entity;

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
 * NegotiationRound Entity
 * 
 * Represents a single round/offer in a multi-round negotiation
 * Tracks chronological history of price counter-offers
 */
@Entity
@Table(name = "negotiation_rounds", indexes = {
    @Index(name = "idx_negotiation_rounds_negotiation", columnList = "negotiation_id"),
    @Index(name = "idx_negotiation_rounds_round", columnList = "negotiation_id, round_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "negotiation")
@EqualsAndHashCode(exclude = "negotiation")
public class NegotiationRound {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negotiation_id", nullable = false)
    private Negotiation negotiation;
    
    @Column(nullable = false)
    private Integer roundNumber;
    
    @Column(nullable = false, length = 20)
    private String offeredBy; // BUYER, SELLER, ADMIN
    
    @Column(length = 100)
    private String offeredByPartyId;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal rate;
    
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
